package com.buschmais.jqassistant.plugin.spring.test.constraint;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.core.analysis.api.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.spring.test.set.controllerservicerepository.ControllerWithServiceAndRepository;
import com.buschmais.jqassistant.plugin.spring.test.set.controllerservicerepository.TestRepository;
import com.buschmais.jqassistant.plugin.spring.test.set.controllerservicerepository.TestService;

public class DependencyStructureIT extends AbstractJavaPluginIT {

    @Test
    public void dependencyStructure() throws Exception {
        scanClasses(ControllerWithServiceAndRepository.class, TestService.class, TestRepository.class);
        assertThat(validateConstraint("spring:ImplementationDependencies").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportWriter.getConstraintResults().values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("spring:ImplementationDependencies")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(1));
        store.commitTransaction();
    }

    @Test
    public void dependencyStructureStrong() throws Exception {
        scanClasses(ControllerWithServiceAndRepository.class, TestService.class, TestRepository.class);
        assertThat(validateConstraint("spring:ImplementationDependenciesStrong").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportWriter.getConstraintResults().values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("spring:ImplementationDependenciesStrong")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(1));
        store.commitTransaction();
    }
}
