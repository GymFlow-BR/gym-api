package br.com.gymflow.api.service;


import br.com.gymflow.api.domain.Exercise;
import br.com.gymflow.api.domain.Organization;
import br.com.gymflow.api.dto.exercise.CreateExerciseRequest;
import br.com.gymflow.api.dto.exercise.ExerciseResponse;
import br.com.gymflow.api.dto.exercise.UpdateExerciseRequest;
import br.com.gymflow.api.exception.ResourceNotFoundException;
import br.com.gymflow.api.mapper.ExerciseMapper;
import br.com.gymflow.api.repository.ExerciseRepository;
import br.com.gymflow.api.repository.OrganizationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final OrganizationRepository organizationRepository;
    private final ExerciseMapper exerciseMapper;

    @Transactional
    public ExerciseResponse create(CreateExerciseRequest request) {
        Organization organization = getOrganizationById(request.organizationId());

        Exercise exercise = exerciseMapper.toEntity(request);
        exercise.setOrganization(organization);

        Exercise savedExercise = exerciseRepository.save(exercise);

        return exerciseMapper.toResponse(savedExercise);
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> findAll() {
        return exerciseRepository.findAll()
                .stream()
                .map(exerciseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseResponse findById(Long id) {
        Exercise exercise = getExerciseById(id);

        return exerciseMapper.toResponse(exercise);
    }

    @Transactional
    public ExerciseResponse update(Long id, UpdateExerciseRequest request) {
        Exercise exercise = getExerciseById(id);

        exerciseMapper.updateEntity(exercise, request);

        Exercise updatedExercise = exerciseRepository.save(exercise);

        return exerciseMapper.toResponse(updatedExercise);
    }

    @Transactional
    public void delete(Long id) {
        Exercise exercise = getExerciseById(id);

        exerciseRepository.delete(exercise);
    }


    private Exercise getExerciseById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with id: " + id));
    }

    private Organization getOrganizationById(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + organizationId));
    }
}