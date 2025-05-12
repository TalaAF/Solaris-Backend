// src/main/java/com/example/lms/calender/assembler/CalendarEventAssembler.java
package com.example.lms.calender.assembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.example.lms.calender.DTO.CalendarEventDTO;
import com.example.lms.calender.controller.CalendarController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CalendarEventAssembler implements RepresentationModelAssembler<CalendarEventDTO, EntityModel<CalendarEventDTO>> {
    @Override
    public EntityModel<CalendarEventDTO> toModel(CalendarEventDTO entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(CalendarController.class).getEventById(entity.getId())).withSelfRel(),
                linkTo(methodOn(CalendarController.class).getEventsByDate(entity.getDate())).withRel("events-by-date"),
                linkTo(methodOn(CalendarController.class).getEvents(null, null, null)).withRel("all-events"));
    }
}