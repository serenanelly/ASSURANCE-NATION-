package com.assurance.nation.service;

import com.assurance.nation.dto.ReimbursementDTO;
import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.MedicalRecord;
import com.assurance.nation.entity.Reimbursement;
import com.assurance.nation.entity.enums.AuditAction;
import com.assurance.nation.entity.enums.ReimbursementStatus;
import com.assurance.nation.entity.enums.TypeConsultation;
import com.assurance.nation.exception.BusinessException;
import com.assurance.nation.exception.ResourceNotFoundException;
import com.assurance.nation.mapper.ReimbursementMapper;
import com.assurance.nation.repository.MedicalRecordRepository;
import com.assurance.nation.repository.ReimbursementRepository;
import com.assurance.nation.security.OwnershipService;
import com.assurance.nation.security.SecurityUtil;
import com.assurance.nation.util.Constants;
import com.assurance.nation.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Logique de remboursement : taux 100% généraliste, 80% spécialiste, workflow PENDING → APPROVED → PAID.
 */
@Service
@RequiredArgsConstructor
public class ReimbursementService {

    private final ReimbursementRepository reimbursementRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordService medicalRecordService;
    private final ReimbursementMapper reimbursementMapper;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final PdfGenerator pdfGenerator;
    private final OwnershipService ownershipService;

    @Transactional
    public ReimbursementDTO.Response create(ReimbursementDTO.CreateRequest request, String ip) {
        MedicalRecord record = medicalRecordService.getEntity(request.getMedicalRecordId());
        if (record.isEstRemboursee()) {
            throw new BusinessException("Cette feuille de maladie est déjà remboursée");
        }
        if (reimbursementRepository.existsByMedicalRecordId(record.getId())) {
            throw new BusinessException("Un remboursement existe déjà pour cette feuille de maladie");
        }
        TypeConsultation type = record.getConsultation().getTypeConsultation();
        int taux = type == TypeConsultation.GENERALISTE ? Constants.TAUX_GENERALISTE : Constants.TAUX_SPECIALISTE;
        BigDecimal montantRembourse = request.getMontantTotal()
                .multiply(BigDecimal.valueOf(taux))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Reimbursement reimbursement = Reimbursement.builder()
                .numRemboursement(generateNumRemboursement())
                .medicalRecord(record)
                .montantTotal(request.getMontantTotal())
                .tauxRemboursement(taux)
                .montantRembourse(montantRembourse)
                .modePaiement(request.getModePaiement())
                .notes(request.getNotes())
                .status(ReimbursementStatus.PENDING)
                .build();

        try {
            String path = pdfGenerator.generateJustificatif(reimbursement);
            reimbursement.setJustificatifPath(path);
        } catch (IOException e) {
            throw new BusinessException("Échec de génération du justificatif PDF");
        }
        reimbursement = reimbursementRepository.save(reimbursement);
        Assure assure = record.getAssure();
        notificationService.notifyReimbursementCreated(reimbursement, assure);
        auditService.log("Reimbursement", reimbursement.getId().toString(), AuditAction.CREATE,
                auditService.findActorByEmail(SecurityUtil.getCurrentUserEmail()), null,
                reimbursementMapper.toResponse(reimbursement), ip);
        return reimbursementMapper.toResponse(reimbursement);
    }

    @Transactional
    public ReimbursementDTO.Response approve(UUID id, String ip) {
        Reimbursement reimbursement = getEntity(id);
        if (reimbursement.getStatus() != ReimbursementStatus.PENDING) {
            throw new BusinessException("Seul un remboursement en attente peut être approuvé");
        }
        reimbursement.setStatus(ReimbursementStatus.APPROVED);
        if (reimbursement.getJustificatifPath() == null) {
            try {
                String path = pdfGenerator.generateJustificatif(reimbursement);
                reimbursement.setJustificatifPath(path);
            } catch (IOException e) {
                throw new BusinessException("Échec de génération du justificatif PDF");
            }
        }
        reimbursement = reimbursementRepository.save(reimbursement);
        notificationService.notifyReimbursementApproved(reimbursement, reimbursement.getMedicalRecord().getAssure());
        auditService.log("Reimbursement", id.toString(), AuditAction.UPDATE,
                auditService.findActorByEmail(SecurityUtil.getCurrentUserEmail()), null,
                reimbursementMapper.toResponse(reimbursement), ip);
        return reimbursementMapper.toResponse(reimbursement);
    }

    @Transactional
    public ReimbursementDTO.Response reject(UUID id, ReimbursementDTO.RejectRequest request, String ip) {
        Reimbursement reimbursement = getEntity(id);
        if (reimbursement.getStatus() != ReimbursementStatus.PENDING) {
            throw new BusinessException("Seul un remboursement en attente peut être rejeté");
        }
        reimbursement.setStatus(ReimbursementStatus.REJECTED);
        reimbursement.setNotes(request.getMotif());
        reimbursement = reimbursementRepository.save(reimbursement);
        notificationService.notifyReimbursementRejected(reimbursement, request.getMotif());
        auditService.log("Reimbursement", id.toString(), AuditAction.UPDATE,
                auditService.findActorByEmail(SecurityUtil.getCurrentUserEmail()), null,
                reimbursementMapper.toResponse(reimbursement), ip);
        return reimbursementMapper.toResponse(reimbursement);
    }

    @Transactional
    public ReimbursementDTO.Response markPaid(UUID id, String ip) {
        Reimbursement reimbursement = getEntity(id);
        if (reimbursement.getStatus() != ReimbursementStatus.APPROVED) {
            throw new BusinessException("Seul un remboursement approuvé peut être marqué comme payé");
        }
        reimbursement.setStatus(ReimbursementStatus.PAID);
        reimbursement.setDateRemboursement(LocalDateTime.now());
        reimbursement = reimbursementRepository.save(reimbursement);

        MedicalRecord record = reimbursement.getMedicalRecord();
        record.setEstRemboursee(true);
        record.setDateRemboursement(LocalDateTime.now());
        record.setMontantRembourse(reimbursement.getMontantRembourse());
        medicalRecordRepository.save(record);

        notificationService.notifyReimbursementPaid(reimbursement, record.getAssure());
        auditService.log("Reimbursement", id.toString(), AuditAction.UPDATE,
                auditService.findActorByEmail(SecurityUtil.getCurrentUserEmail()), null,
                reimbursementMapper.toResponse(reimbursement), ip);
        return reimbursementMapper.toResponse(reimbursement);
    }

    private String generateNumRemboursement() {
        int year = Year.now().getValue();
        String prefix = Constants.REMBOURSEMENT_PREFIX + "-" + year + "-";
        long count = reimbursementRepository.countByNumPrefix(prefix) + 1;
        return prefix + String.format("%06d", count);
    }

    @Transactional(readOnly = true)
    public ReimbursementDTO.PageResponse findAll(
            int page, int size, ReimbursementStatus status, UUID assureId,
            LocalDate startDate, LocalDate endDate) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;
        Page<Reimbursement> result = reimbursementRepository.findFiltered(status, assureId, start, end, pageable);
        List<ReimbursementDTO.Response> content = result.getContent().stream()
                .map(reimbursementMapper::toResponse).toList();
        ReimbursementDTO.PageResponse response = new ReimbursementDTO.PageResponse();
        response.setContent(content);
        response.setTotalElements(result.getTotalElements());
        response.setTotalPages(result.getTotalPages());
        response.setPage(result.getNumber());
        response.setSize(result.getSize());
        response.setStatistics(buildListStatistics(result.getContent()));
        return response;
    }

    @Transactional(readOnly = true)
    public ReimbursementDTO.PageResponse findForCurrentPatient(int page, int size) {
        Assure assure = ownershipService.getCurrentAssure();
        PageRequest pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Reimbursement> result = reimbursementRepository.findByAssureId(assure.getId(), pageable);
        List<ReimbursementDTO.Response> content = result.getContent().stream()
                .map(reimbursementMapper::toResponse).toList();
        ReimbursementDTO.PageResponse response = new ReimbursementDTO.PageResponse();
        response.setContent(content);
        response.setTotalElements(result.getTotalElements());
        response.setTotalPages(result.getTotalPages());
        response.setPage(result.getNumber());
        response.setSize(result.getSize());
        return response;
    }

    private ReimbursementDTO.ListStatistics buildListStatistics(List<Reimbursement> items) {
        ReimbursementDTO.ListStatistics stats = new ReimbursementDTO.ListStatistics();
        stats.setNombreRemboursements(items.size());
        BigDecimal total = items.stream()
                .map(Reimbursement::getMontantRembourse)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalRembourses(total);
        if (!items.isEmpty()) {
            stats.setMoyenneParRemboursement(total.divide(BigDecimal.valueOf(items.size()), 2, RoundingMode.HALF_UP));
        } else {
            stats.setMoyenneParRemboursement(BigDecimal.ZERO);
        }
        return stats;
    }

    @Transactional(readOnly = true)
    public ReimbursementDTO.Response findById(UUID id) {
        Reimbursement r = getEntity(id);
        ownershipService.assertCanAccessReimbursement(r);
        return reimbursementMapper.toResponse(r);
    }

    @Transactional(readOnly = true)
    public ReimbursementDTO.DashboardResponse dashboard() {
        ReimbursementDTO.DashboardResponse dto = new ReimbursementDTO.DashboardResponse();
        dto.setTotalRemboursements(reimbursementRepository.count());
        dto.setPendingCount(reimbursementRepository.countByStatus(ReimbursementStatus.PENDING));
        dto.setApprovedCount(reimbursementRepository.countByStatus(ReimbursementStatus.APPROVED));
        dto.setPaidCount(reimbursementRepository.countByStatus(ReimbursementStatus.PAID));
        dto.setRejectedCount(reimbursementRepository.countByStatus(ReimbursementStatus.REJECTED));
        dto.setMontantTotalPaye(reimbursementRepository.sumMontantPaye());
        dto.setTotalMontants(dto.getMontantTotalPaye());
        dto.setRembourseParMois(buildMonthlyStats());
        dto.setRembourseParSpecialite(buildSpecialiteStats());
        return dto;
    }

    private List<ReimbursementDTO.MonthlyStat> buildMonthlyStats() {
        List<Reimbursement> paid = reimbursementRepository.findByStatus(ReimbursementStatus.PAID);
        Map<YearMonth, ReimbursementDTO.MonthlyStat> byMonth = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        for (Reimbursement r : paid) {
            if (r.getDateRemboursement() == null) continue;
            YearMonth ym = YearMonth.from(r.getDateRemboursement());
            ReimbursementDTO.MonthlyStat stat = byMonth.computeIfAbsent(ym, k -> {
                ReimbursementDTO.MonthlyStat s = new ReimbursementDTO.MonthlyStat();
                s.setMois(k.format(fmt));
                s.setMontant(BigDecimal.ZERO);
                s.setCount(0);
                return s;
            });
            stat.setMontant(stat.getMontant().add(r.getMontantRembourse()));
            stat.setCount(stat.getCount() + 1);
        }
        return new ArrayList<>(byMonth.values());
    }

    private Map<String, ReimbursementDTO.SpecialiteStat> buildSpecialiteStats() {
        List<Reimbursement> paid = reimbursementRepository.findByStatus(ReimbursementStatus.PAID);
        Map<String, BigDecimal> montants = new HashMap<>();
        montants.put("GENERALISTE", BigDecimal.ZERO);
        montants.put("SPECIALISTE", BigDecimal.ZERO);
        for (Reimbursement r : paid) {
            TypeConsultation type = r.getMedicalRecord().getConsultation().getTypeConsultation();
            String key = type.name();
            montants.merge(key, r.getMontantRembourse(), BigDecimal::add);
        }
        BigDecimal total = montants.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, ReimbursementDTO.SpecialiteStat> result = new HashMap<>();
        for (Map.Entry<String, BigDecimal> e : montants.entrySet()) {
            ReimbursementDTO.SpecialiteStat stat = new ReimbursementDTO.SpecialiteStat();
            stat.setMontant(e.getValue());
            stat.setTaux(total.compareTo(BigDecimal.ZERO) > 0
                    ? e.getValue().multiply(BigDecimal.valueOf(100))
                            .divide(total, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0);
            result.put(e.getKey(), stat);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Resource downloadJustificatif(UUID id) throws IOException {
        Reimbursement r = getEntity(id);
        ownershipService.assertCanAccessReimbursement(r);
        if (r.getJustificatifPath() == null) {
            throw new ResourceNotFoundException("Justificatif non disponible");
        }
        Path path = Paths.get(r.getJustificatifPath());
        return new UrlResource(path.toUri());
    }

    public Reimbursement getEntity(UUID id) {
        return reimbursementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remboursement introuvable"));
    }
}
