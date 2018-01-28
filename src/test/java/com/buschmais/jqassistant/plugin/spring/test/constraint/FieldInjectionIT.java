package com.buschmais.jqassistant.plugin.spring.test.constraint;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.stereotype.Component;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.spring.test.set.fieldinjection.ServiceWithConstructorInjection;
import com.buschmais.jqassistant.plugin.spring.test.set.fieldinjection.ServiceWithFieldInjection;

public class FieldInjectionIT extends AbstractJavaPluginIT {

    @Test
    public void serviceWithFieldInjection() throws Exception {
        scanClasses(ServiceWithFieldInjection.class);
        verifyConstraintResult(ServiceWithFieldInjection.class, "repository");
    }

    @Test
    public void serviceWithConstructorInjection() throws Exception {
        scanClasses(ServiceWithConstructorInjection.class);
        assertThat(validateConstraint("spring-injection:FieldInjectionIsNotAllowed").getStatus(), equalTo(SUCCESS));
    }

    @Test
    public void rejectsNonFinalInjectableField() throws Exception {

        scanClasses(ServiceWithConstructorInjection.class);

        Result<Constraint> constraint = validateConstraint("spring-injection:InjectablesShouldBeHeldInFinalFields");

        assertThat(constraint.getStatus(), equalTo(FAILURE));

        String string = constraint.getRows().get(0).get("Message").toString();
        assertThat(string, containsString(ServiceWithConstructorInjection.class.getName()));
        assertThat(string, containsString("repository"));
    }
    
    @Test
    public void doesNotRejectFinalInjectableField() throws Exception {
    	
        scanClasses(SomeComponent.class);
        
        assertThat(validateConstraint("spring-injection:InjectablesShouldBeHeldInFinalFields").getStatus(), is(SUCCESS));
    }

    private void verifyConstraintResult(Class<?> type, String fieldName) throws Exception {
        assertThat(validateConstraint("spring-injection:FieldInjectionIsNotAllowed").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportWriter.getConstraintResults().values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("spring-injection:FieldInjectionIsNotAllowed")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(1));
        Map<String, Object> row = rows.get(0);
        TypeDescriptor typeDescriptor = (TypeDescriptor) row.get("Type");
        FieldDescriptor fieldDescriptor = (FieldDescriptor) row.get("Field");
        assertThat(typeDescriptor, typeDescriptor(type));
        assertThat(fieldDescriptor, fieldDescriptor(type, fieldName));
        store.commitTransaction();
    }
    
    @Component
    static class SomeComponent {
    	
        private final SomeComponent dependency;
        
        public SomeComponent(SomeComponent dependency) {
            this.dependency = dependency;
        }
    }
}
