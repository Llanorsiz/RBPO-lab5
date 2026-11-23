package kurban.softwareProducts.developmentTask.service;

import kurban.softwareProducts.common.exception.base.ConflictException;
import kurban.softwareProducts.common.exception.base.ForbiddenException;
import kurban.softwareProducts.common.exception.base.NotFoundException;
import kurban.softwareProducts.developmentTask.exception.DevelopmentTaskNotFoundException;
import kurban.softwareProducts.developmentTask.mapper.DevelopmentTaskMapper;
import kurban.softwareProducts.developmentTask.model.DevelopmentTask;
import kurban.softwareProducts.developmentTask.model.DevelopmentTaskProgress;
import kurban.softwareProducts.developmentTask.repository.DevelopmentTaskRepository;
import kurban.softwareProducts.developmentTask.web.request.CreateDevelopmentTaskRequest;
import kurban.softwareProducts.developmentTask.web.request.UpdateDevelopmentTaskRequest;
import kurban.softwareProducts.developmentTask.web.response.DevelopmentTaskResponse;
import kurban.softwareProducts.user.exception.UserNotFoundException;
import kurban.softwareProducts.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static kurban.softwareProducts.common.security.SecurityUtils.*;

@Service
@RequiredArgsConstructor
public class DevelopmentTaskService {
    private final DevelopmentTaskMapper developmentTaskMapper;
    private final UserRepository userRepository;

    private final DevelopmentTaskRepository developmentTaskRepository;

    public List<DevelopmentTaskResponse> getDevelopmentTasks() {
        return developmentTaskRepository.findAll()
                .stream()
                .map(developmentTaskMapper::toDevelopmentTaskResponse)
                .toList();
    }

    public DevelopmentTaskResponse getDevelopmentTask(Long developmentTaskId){
        return developmentTaskRepository.findById(developmentTaskId)
                .map(developmentTaskMapper::toDevelopmentTaskResponse)
                .orElseThrow(DevelopmentTaskNotFoundException::new);
    }

    @Transactional
    public DevelopmentTaskResponse completeTask(Long taskId){
        DevelopmentTask developmentTask = developmentTaskRepository.findById(taskId)
                .orElseThrow(DevelopmentTaskNotFoundException::new);

        if(!isAdmin() && !getCurrentUserId().equals(developmentTask.getAssigneeUserId())){
            throw new ForbiddenException();
        }
        if(developmentTask.getProgress() != DevelopmentTaskProgress.IN_PROGRESS){
            throw new ConflictException("Завершить задачу можно, когда она в процессе");
        }

        developmentTask.setProgress(DevelopmentTaskProgress.DONE);
        return developmentTaskMapper.toDevelopmentTaskResponse(developmentTask);
    }

    @Transactional
    public DevelopmentTaskResponse assignTask(Long taskId, Long userId){
        DevelopmentTask developmentTask = developmentTaskRepository.findById(taskId)
                .orElseThrow(DevelopmentTaskNotFoundException::new);

        if(!isAdmin() && !getCurrentUserId().equals(userId)){
            throw new ForbiddenException("Вы можете назначать задачи только на себя");
        }

        if(developmentTask.getProgress() == DevelopmentTaskProgress.DONE){
            throw new ConflictException("Нельзя назначить исполнителя уже на выполненную задачу");
        }

        if(!userRepository.existsById(userId)){
            throw new UserNotFoundException();
        }

        developmentTask.setAssigneeUserId(userId);
        return developmentTaskMapper.toDevelopmentTaskResponse(developmentTask);
    }

    @Transactional
    public DevelopmentTaskResponse createDevelopmentTask(CreateDevelopmentTaskRequest request) {
        DevelopmentTask developmentTask = developmentTaskMapper.fromCreateDevelopmentTaskRequest(request);
        developmentTaskRepository.save(developmentTask);
        return developmentTaskMapper.toDevelopmentTaskResponse(developmentTask);
    }

    @Transactional
    public DevelopmentTaskResponse updateDevelopmentTask(Long developmentTaskId, UpdateDevelopmentTaskRequest request) {
        DevelopmentTask developmentTask = developmentTaskRepository.findById(developmentTaskId)
                .orElseThrow(DevelopmentTaskNotFoundException::new);

        if(!isAdmin() && !getCurrentUserId().equals(developmentTask.getAssigneeUserId())){
            throw new ForbiddenException("Вы можете изменять только свои задачи");
        }

        developmentTaskMapper.updateFromUpdateDevelopmentTaskRequest(request, developmentTask);
        return developmentTaskMapper.toDevelopmentTaskResponse(developmentTask);
    }

    @Transactional
    public void deleteDevelopmentTask(Long developmentTaskId) {
        developmentTaskRepository.deleteById(developmentTaskId);
    }
}