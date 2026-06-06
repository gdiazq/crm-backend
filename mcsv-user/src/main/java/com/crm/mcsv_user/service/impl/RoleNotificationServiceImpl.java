package com.crm.mcsv_user.service.impl;

import com.crm.common.dto.SendNotificationRequest;
import com.crm.mcsv_user.entity.User;
import com.crm.mcsv_user.event.NotificationBatchEvent;
import com.crm.mcsv_user.repository.RoleRepository;
import com.crm.mcsv_user.repository.UserRepository;
import com.crm.mcsv_user.service.RoleNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleNotificationServiceImpl implements RoleNotificationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void notifyRoleUpdated(Long roleId, String roleName) {
        List<SendNotificationRequest> notifications = userRepository.findAllByRolesId(roleId).stream()
                .map(u -> SendNotificationRequest.builder()
                        .userId(u.getId())
                        .title("Rol actualizado")
                        .message("Tu rol \"" + roleName + "\" ha sido actualizado por un administrador.")
                        .type("INFO")
                        .build())
                .collect(Collectors.toList());
        publish(notifications);
    }

    @Override
    public void notifyRoleStatusChanged(Long roleId, String roleName, boolean enabled) {
        List<User> users = userRepository.findAllByRolesId(roleId);

        String statusLabel = enabled ? "activado" : "desactivado";
        String userTitle   = enabled ? "Cuenta reactivada" : "Cuenta desactivada";
        String userMessage = enabled
                ? "Tu cuenta ha sido reactivada porque el rol \"" + roleName + "\" fue activado."
                : "Tu cuenta ha sido desactivada porque el rol \"" + roleName + "\" fue desactivado.";
        String userType = enabled ? "SUCCESS" : "WARNING";

        List<SendNotificationRequest> notifications = new ArrayList<>();
        users.forEach(u -> notifications.add(SendNotificationRequest.builder()
                .userId(u.getId())
                .title(userTitle)
                .message(userMessage)
                .type(userType)
                .build()));

        roleRepository.findByName("ROLE_ADMIN").ifPresent(adminRole ->
                userRepository.findAllByRolesId(adminRole.getId()).forEach(admin ->
                        notifications.add(SendNotificationRequest.builder()
                                .userId(admin.getId())
                                .title("Rol " + statusLabel)
                                .message("El rol \"" + roleName + "\" ha sido " + statusLabel + ". " + users.size() + " usuario(s) afectado(s).")
                                .type("INFO")
                                .build())));

        publish(notifications);
    }

    @Override
    public void notifyPermissionsChanged(Long roleId, String roleName, String userMessage) {
        List<User> users = userRepository.findAllByRolesId(roleId);

        List<SendNotificationRequest> notifications = new ArrayList<>();
        users.forEach(u -> notifications.add(SendNotificationRequest.builder()
                .userId(u.getId())
                .title("Permisos actualizados")
                .message(userMessage)
                .type("INFO")
                .build()));

        roleRepository.findByName("ROLE_ADMIN").ifPresent(adminRole ->
                userRepository.findAllByRolesId(adminRole.getId()).forEach(admin ->
                        notifications.add(SendNotificationRequest.builder()
                                .userId(admin.getId())
                                .title("Permisos de rol actualizados")
                                .message("Los permisos del rol \"" + roleName + "\" han sido modificados. " + users.size() + " usuario(s) afectado(s).")
                                .type("INFO")
                                .build())));

        publish(notifications);
    }

    private void publish(List<SendNotificationRequest> notifications) {
        if (!notifications.isEmpty()) {
            eventPublisher.publishEvent(new NotificationBatchEvent(notifications));
        }
    }
}
