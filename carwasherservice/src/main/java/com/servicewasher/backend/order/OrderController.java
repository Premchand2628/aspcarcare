package com.servicewasher.backend.order;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  @GetMapping("/assigned")
  public List<AssignedOrderDto> assigned() {
    return List.of(
      new AssignedOrderDto("10:00AM", "Supriya", "*****0125"),
      new AssignedOrderDto("11:00AM", "Prem chand", "*****0125"),
      new AssignedOrderDto("01:00PM", "Prem chand", "*****0125")
    );
  }

  public record AssignedOrderDto(String time, String name, String phone) {}
}