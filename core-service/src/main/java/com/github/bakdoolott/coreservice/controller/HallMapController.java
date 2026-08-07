package com.github.bakdoolott.coreservice.controller;

//import com.github.bakdoolott.coreservice.models.dto.HallMapDto;
//import com.github.bakdoolott.coreservice.response.GlobalResponse;
//import com.github.bakdoolott.coreservice.services.HallMapService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//
//@RestController
//@RequestMapping("/api/v1/core/halls")
//@Tag(name = "Hall Map Controller", description = "Карта зала со столиками")
//public class HallMapController {
//    private final HallMapService hallMapService;
//
//    public HallMapController(HallMapService hallMapService) {
//        this.hallMapService = hallMapService;
//    }
//
//    @GetMapping("/{hallId}/map")
//    @Operation(summary = "Карта зала со столиками на выбранную дату")
//    public ResponseEntity<GlobalResponse> getHallMap(@PathVariable Long hallId,
//                                                     @RequestParam(required = false)
//                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
//        HallMapDto mapDto = hallMapService.getHallMap(hallId,date);
//        return new ResponseEntity<>(mapDto, )
//    }
//}
