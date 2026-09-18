package com.bff.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SliceDTO<T> {

    private List<T> content;

    private int page;

    private int size;

    private boolean hasNext;

}
