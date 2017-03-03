package com.axelor.di;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import groovy.lang.DelegatesTo.Target;


@Retention(RetentionPolicy.RUNTIME) @java.lang.annotation.Target(ElementType.METHOD)
public @interface LicenceRequired {}