package de.waldorfaugsburg.lessoncontrol.common.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class BBBServiceConfiguration extends AbstractServiceConfiguration {

    private String driverPath;
    private String email;
    private String password;

}
