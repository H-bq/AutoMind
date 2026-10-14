package com.hs.common.advice;


import com.hs.common.domain.R;
import com.hs.common.exception.BadRequestException;
import com.hs.common.exception.CommonException;
import com.hs.common.exception.DbException;
import com.hs.common.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.BindException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class CommonExceptionAdvice {

    @ExceptionHandler(DbException.class)
    public Object handleDbException(DbException e) {
        log.error("mysql数据库操作异常 -> ", e);
        return processResponse(new CommonException("数据处理异常，请稍后重试", 500));
    }

    @ExceptionHandler(CommonException.class)
    public Object handleBadRequestException(CommonException e) {
        log.error("自定义异常 -> {} , 异常原因：{}  ", e.getClass().getName(), e.getMessage());
        log.debug("", e);
        return processResponse(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors()
                .stream().map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        log.error("请求参数校验异常 -> {}", msg);
        log.debug("", e);
        return processResponse(new BadRequestException(msg));
    }

    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException e) {
        log.error("请求参数绑定异常 -> BindException，{}", e.getMessage());
        log.debug("", e);
        return processResponse(new BadRequestException("请求参数格式错误"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingParam(MissingServletRequestParameterException e) {
        log.error("缺少请求参数 -> {}", e.getMessage());
        return processResponse(new BadRequestException("缺少必要参数：" + e.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Object handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("参数类型不匹配 -> {}", e.getMessage());
        return processResponse(new BadRequestException("参数类型错误：" + e.getName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("请求体解析失败 -> {}", e.getMessage());
        return processResponse(new BadRequestException("请求数据格式不正确，请检查后重试"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.error("请求方法不支持 -> {}", e.getMessage());
        return processResponse(new CommonException("不支持该请求方式：" + e.getMethod(), 405));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Object handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.error("不支持的媒体类型 -> {}", e.getMessage());
        return processResponse(new CommonException("不支持的请求内容类型", 415));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(NoResourceFoundException e) {
        log.error("资源未找到 -> {}", e.getMessage());
        return processResponse(new CommonException("请求的资源不存在", 404));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.error("上传文件过大 -> {}", e.getMessage());
        return processResponse(new BadRequestException("上传文件大小超出限制，请压缩后重试"));
    }

    @ExceptionHandler(Exception.class)
    public Object handleRuntimeException(Exception e) {
        HttpServletRequest request = WebUtils.getRequest();
        String uri = request != null ? request.getRequestURI() : "unknown";
        log.error("其他异常 uri : {} -> ", uri, e);
//        String eMessage = e.getMessage();
        return processResponse(new CommonException(e.getMessage(), 500));
    }

    private ResponseEntity<R<Void>> processResponse(CommonException e) {
        return ResponseEntity.status(e.getCode()).body(R.error(e));
    }
}
