package com.microsave.service;

import com.microsave.dto.GroupRequest;
import com.microsave.entity.Group;
import com.microsave.exception.ResourceNotFoundException;
import com.microsave.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public Group createGroup(GroupRequest request) {
        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedDate(request.getCreatedDate() != null ? request.getCreatedDate() : LocalDate.now());
        return groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Group getGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group with ID " + id + " not found."));
    }

    public Group updateGroup(Long id, GroupRequest request) {
        Group group = getGroupById(id);
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        if (request.getCreatedDate() != null) {
            group.setCreatedDate(request.getCreatedDate());
        }
        return groupRepository.save(group);
    }

    public void deleteGroup(Long id) {
        Group group = getGroupById(id);
        groupRepository.delete(group);
    }
}
