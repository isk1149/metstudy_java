package com.example.demo.service;

import com.example.demo.dto.TodoDTO;
import com.example.demo.model.TodoEntity;
import com.example.demo.persistence.TodoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TodoService {
    @Autowired
    private TodoRepository repository;

    //private final String userId = "tempUser";

    public String testService() {
        // TodoEntity 생성
        TodoEntity entity = TodoEntity.builder().title("My first todo item").build();
        // TodoEntity 저장
        repository.save(entity);
        // TodoEntity 검색
        TodoEntity savedEntity = repository.findById(entity.getId()).get();
        return savedEntity.getTitle();
    }

    public String testService2() {
        // TodoEntity 생성
        TodoEntity entity = TodoEntity.builder().title("My first todo item22").build();
        // TodoEntity 저장
        repository.save(entity);
        // TodoEntity 검색
        List<TodoEntity> byUserId = repository.findByUserId(entity.getId());
        return byUserId.get(0).getTitle();
    }

/*    public List<TodoDTO> select(String id){

        TodoEntity selectedEntity = repository.findById(id).get();
        TodoDTO dto = new TodoDTO(selectedEntity);

        *//*Optional<TodoEntity> entity = repository.findById(id);
        return entity.orElse(new TodoEntity());*//*
        return dto;
    }*/

    public List<TodoDTO> selectAll() {
        List<TodoEntity> allEntities = repository.findAll();
        List<TodoDTO> dtos = allEntities.stream().map(TodoDTO::new).collect(Collectors.toList());
        return dtos;
    }

    public List<TodoDTO> create(TodoDTO dto, String userId) {
        TodoEntity entity = TodoDTO.toEntity(dto);
        entity.setUserId(userId);
        repository.save(entity);
        List<TodoEntity> arrEntity = repository.findByUserId(entity.getUserId());
        List<TodoDTO> list = arrEntity.stream().map(TodoDTO::new).collect(Collectors.toList());
        //List<TodoDTO> list = arrEntity.stream().map(item -> {return new TodoDTO(item);}).collect(Collectors.toList());

        return list;
    }

    public List<TodoDTO> retrieve(String userId) {

        List<TodoEntity> entity = repository.findByUserId(userId);
        List<TodoDTO> list = entity.stream().map(TodoDTO::new).collect(Collectors.toList());

        test(dto -> {
            dto.setTitle("title");
            System.out.println("dto = " + dto);
        });

        return list;
    }

    public List<TodoDTO> update(TodoDTO dto, String userId) {
        validate(dto);

        final Optional<TodoEntity> original = repository.findById(dto.getId());
        original.ifPresent(entity -> {
            entity.setTitle(dto.getTitle());
            entity.setDone(dto.isDone());

            repository.save(entity);
        });

        List<TodoDTO> listDto = this.retrieve(userId);

        return listDto;
    }

    public List<TodoDTO> delete(String id, String userId) {
        TodoEntity entity = TodoEntity.builder().id(id).build();
        repository.delete(entity);
        List<TodoDTO> list = this.retrieve(userId);
        return list;
    }

    public void test(Consumer<TodoDTO> consumer) {
        consumer.accept(new TodoDTO());
    }

    private void validate(TodoDTO dto) {
        if (dto == null) {
            log.error("dto is null");
            new RuntimeException("dto is null");
        }
    }

    /*public TodoDTO create(TodoDTO dto) {
        TodoEntity entity = TodoDTO.toEntity(dto);
        repository.save(entity);
        TodoEntity savedEntity = repository.findById(entity.getId()).get();
        TodoDTO savedDto = new TodoDTO(savedEntity);
        return savedDto;
    }*/

/*    public TodoDTO update(TodoDTO dto) {
        TodoEntity selectedEntity = repository.findById(dto.getId()).get();
        if (selectedEntity != null) {
            selectedEntity.setTitle(dto.getTitle());
            selectedEntity.setDone(dto.isDone());
            repository.save(selectedEntity);
            return new TodoDTO(repository.findById(selectedEntity.getId()).get());
        }
        return TodoDTO.builder().title("수정할 객체가 없음").done(false).build();
    }*/

/*    public TodoDTO delete(String id) {
        //TodoEntity entity = TodoEntity.builder().id(dto.getId()).title(dto.getTitle()).done(dto.isDone()).build();
        TodoEntity selectedEntity = repository.findById(id).get();
        if (selectedEntity != null) {
            repository.delete(selectedEntity);
            return TodoDTO.builder().title("삭제완료").build();
        }
        return TodoDTO.builder().title("삭제할 객체가 없음").build();
    }*/


/*
    public List<TodoEntity> create(final TodoEntity entity) {
        //validations
        validate(entity);

        repository.save(entity);
        log.info("Entity Id : {} is saved.", entity.getId());

        return repository.findByUserId(entity.getUserId());
    }

    public List<TodoEntity> retrieve(final String userId) {
        return repository.findByUserId(userId);
    }

    public List<TodoEntity> update(final TodoEntity entity) {
        // (1) 저장할 엔티티가 유효한지 확인한다.
        validate(entity);

        // (2) 넘겨받은 엔티티 id를 이용해 TodoEntity를 가져온다. 존재하지 않는 엔티티는 업데이트할 수 없기 때문이다.
        final Optional<TodoEntity> original = repository.findById(entity.getId());
        original.ifPresent(todo -> {
            // (3) 변환된 TodoEntitiy가 존재하면 값을 새 entity 값으로 덮어 씌운다.
            todo.setTitle(entity.getTitle());
            todo.setDone(entity.isDone());

            // (4) 데이터베이스에 새 값을 저장한다.
            repository.save(todo);
        });

        // 유저의 모든 Todo 리스트를 리턴한다.
        return retrieve(entity.getUserId());
    }

    public List<TodoEntity> delete(final TodoEntity entity) {
        // (1) 저장할 엔티티가 유효한지 확인한다.
        validate(entity);

        try {
            // (2) 엔티티를 삭제한다.
            repository.delete(entity);
        } catch(Exception e) {
            // (3) exception 발생 시 id와 exception을 로깅한다.
            log.error("error deleting entity", entity.getId(), e);

            // (4) 컨트롤러로 exception을 날린다. 데이터베이스 내부 로직을 캡슐화하기 위해 e를 리턴하지 않고
            // 새 exception 오브젝트를 리턴한다.
            throw new RuntimeException("error deleting entity" + entity.getId());
        }
        // (5) 새 Todo 리스트를 가져와 리턴한다.
        return retrieve(entity.getUserId());
    }

    private static void validate(TodoEntity entity) {
        if (entity == null) {
            log.warn("Entity cannot be null.");
            throw new RuntimeException("Entity cannot be null.");
        }

        if (entity.getUserId() == null) {
            log.warn("Unknown user.");
            throw new RuntimeException("Unknown user.");
        }
    }*/
}