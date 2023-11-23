package com.facebook.dto.groups;

import com.facebook.model.groups.GroupRole;
import lombok.Data;

@Data
public class GroupMembershipDto {
    private Long id;
    private UserGroup user;
    private GroupRole[] roles;
}
