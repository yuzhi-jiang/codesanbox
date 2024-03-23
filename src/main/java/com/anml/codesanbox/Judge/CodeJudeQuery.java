package com.anml.codesanbox.Judge;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@NoArgsConstructor
public class CodeJudeQuery {
    @NonNull
    String code;
    String language;
    List<String> inputList;
    long timeLimit; // 毫秒
    long memoryLimit; // 字节


}
