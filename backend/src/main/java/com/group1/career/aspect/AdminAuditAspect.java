package com.group1.career.aspect;

import com.group1.career.model.entity.AdminAuditLog;
import com.group1.career.repository.AdminAuditLogRepository;
import com.group1.career.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * F19: AOP aspect that persists an {@link AdminAuditLog} row after every
 * admin controller method annotated with {@link AuditLog} returns
 * successfully. Failures in this aspect are silently swallowed so they
 * never break the primary operation.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminAuditAspect {

    private final AdminAuditLogRepository auditLogRepository;

    @AfterReturning("@annotation(auditLog)")
    public void afterReturning(JoinPoint jp, AuditLog auditLog) {
        try {
            Long adminId = SecurityUtil.currentUserId();
            if (adminId == null) adminId = -1L;

            String targetId = null;
            if (auditLog.idParamIndex() >= 0) {
                Object[] args = jp.getArgs();
                if (args.length > auditLog.idParamIndex()) {
                    Object arg = args[auditLog.idParamIndex()];
                    if (arg != null) targetId = arg.toString();
                }
            }

            String ip = null, ua = null;
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                ip = attrs.getRequest().getRemoteAddr();
                ua = attrs.getRequest().getHeader("User-Agent");
                if (ua != null && ua.length() > 255) ua = ua.substring(0, 255);
            }

            auditLogRepository.save(AdminAuditLog.builder()
                    .adminId(adminId)
                    .action(auditLog.action())
                    .targetType(auditLog.targetType())
                    .targetId(targetId)
                    .ip(ip)
                    .ua(ua)
                    .build());

        } catch (Exception e) {
            log.warn("[audit] failed to persist audit log entry for action={}: {}",
                    auditLog.action(), e.getMessage());
        }
    }
}
