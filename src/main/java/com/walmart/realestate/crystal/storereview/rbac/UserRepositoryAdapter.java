package com.walmart.realestate.crystal.storereview.rbac;

import com.walmart.core.realestate.cerberus.bean.CerberusUserInformation;
import com.walmart.realestate.crystal.storereview.service.UserAccountService;
import com.walmart.realestate.soteria.ad.ADUserRepository;
import com.walmart.realestate.soteria.model.Persona;
import com.walmart.realestate.soteria.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class UserRepositoryAdapter implements ADUserRepository {

    private final UserAccountService userAccountService;

    @Override
    public List<User> getByUsers(CerberusUserInformation userInformation, List<User> users) {
        List<String> loginIds = users.stream()
                .map(User::getLoginId)
                .collect(Collectors.toList());
        return new ArrayList<>(userAccountService.findByLoginIds(loginIds).values());
    }

    @Override
    public User getById(CerberusUserInformation userInformation, String userId) {
        return userAccountService.getUser(userId);
    }

    @Override
    public List<User> getUsersByPersonas(CerberusUserInformation userInformation, List<Persona> personas) {
        List<String> memberships = personas.stream()
                .map(Persona::getMembership)
                .collect(Collectors.toList());
        return userAccountService.findByMemberships(memberships);
    }

}
