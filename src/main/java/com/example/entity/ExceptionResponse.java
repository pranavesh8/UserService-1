package com.example.entity;
import java.time.LocalDateTime;


import java.time.LocalTime;
import java.util.Date;
import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ExceptionResponse {

    private String message;
    private LocalTime time;
    private Date date;
    private HttpStatus status;
    private boolean succress;
   
	}