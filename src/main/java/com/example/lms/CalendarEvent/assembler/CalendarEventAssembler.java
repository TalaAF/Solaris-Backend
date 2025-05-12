package com.example.lms.CalendarEvent.assembler;

import com.example.lms.CalendarEvent.controller.CalendarController;
import com.example.lms.CalendarEvent.DTO.CalendarEventDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CalendarEventAssembler implements RepresentationModelAssembler<CalendarEventDTO, EntityModel<CalendarEventDTO>> {
    @Override
    public EntityModel<CalendarEventDTO> toModel(CalendarEventDTO entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(CalendarController.class).getDayEvents(entity.getDate())).withSelfRel(),
                linkTo(methodOn(CalendarController.class).createEvent(entity)).withRel("create"));
    }
}