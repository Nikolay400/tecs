package com.kolya.tecs.controller.utils;

import org.apache.logging.log4j.util.Strings;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Map;
import java.util.stream.Collectors;

public class ControllerUtils {
    static public Float getPriceFromForm(String key, MultiValueMap<String, String> form){
        if ((form==null)||(form.get(key)==null)|| Strings.isEmpty(form.get(key).get(0))){
            return null;
        }
        return Float.parseFloat(form.get(key).get(0));
    }

    static public Map<String,String> getErrors(BindingResult bindingResult){
        return bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        err->err.getField()+"Error",
                        FieldError::getDefaultMessage));
    }
}
