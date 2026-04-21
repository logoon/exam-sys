package com.exam.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageDTO<T> {
    private List<T> records;
    private Long total;
    private Long current;
    private Long size;
    private Long pages;
}
