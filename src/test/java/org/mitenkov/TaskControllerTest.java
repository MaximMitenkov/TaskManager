package org.mitenkov;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mitenkov.dto.TaskAddRequest;
import org.mitenkov.dto.TaskDto;
import org.mitenkov.entity.Task;
import org.mitenkov.enums.Priority;
import org.mitenkov.enums.TaskType;
import org.mitenkov.helper.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskControllerTest extends BaseTest {

    @Autowired
    EntityGenerator entityGenerator;

    @Autowired
    TaskClient taskClient;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DBCleaner dbCleaner;

    @Autowired
    AuthTestHolder authHolder;

    @BeforeEach
    public void beforeEach() {
        dbCleaner.reset();
        entityGenerator.generateTasksAndSave();
        authHolder.setCurrentUser();
    }

    @Test
    void addTaskTest() throws Exception {

        TaskAddRequest taskAddRequest = new TaskAddRequest(
                "Test",
                "2.1.1",
                LocalDate.now().plusDays(30),
                Priority.LOW,
                TaskType.BUG
        );

        TaskDto result = taskClient.create(taskAddRequest);

        assertEquals(result.title(), taskAddRequest.title());
        assertEquals(result.version(), taskAddRequest.version());
        assertEquals(result.deadline(), taskAddRequest.deadline());
        assertEquals(result.priority(), taskAddRequest.priority());
        assertEquals(result.taskType(), taskAddRequest.type());

        TaskDto dto = taskClient.getById(result.id());
        assertEquals(dto, result);
    }

    @Test
    void deleteTaskTest() throws Exception {

        TaskAddRequest taskAddRequest = new TaskAddRequest(
                "Test",
                "2.1.1",
                LocalDate.now().plusDays(30),
                Priority.LOW,
                TaskType.BUG
        );

        TaskDto result = taskClient.create(taskAddRequest);
        taskClient.deleteById(result.id());

        int status = taskClient.getByIdStatus(result.id());
        assertEquals(404, status);
    }

    @Test
    void getTaskTest() throws Exception {

        TaskConverter converter = new TaskConverter();
        List<Task> tasks = entityGenerator.generateTasks();
        List<TaskAddRequest> tasksToAdd = tasks.stream()
                .map(converter::toAddRequest)
                .toList();

        for (TaskAddRequest t : tasksToAdd) {
            taskClient.create(t);
        }

        List<TaskAddRequest> assertTaskDidNotChange = taskClient.getAll().stream().map(converter::toAddRequest).toList();

        assertEquals(getSet(tasksToAdd), getSet(assertTaskDidNotChange));

    }
}
