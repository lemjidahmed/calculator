package com.coralogix.calculator.services;

import com.coralogix.calculator.model.Result;
import org.springframework.stereotype.Service;

@Service
public class AddService {
    public Result add(int a, int b) {
        int sum = a;
        return new Result(sum);
    }
}
