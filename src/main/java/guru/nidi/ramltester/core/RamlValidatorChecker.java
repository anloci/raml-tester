/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.core;

import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;
import org.raml.model.*;
import org.raml.model.parameter.AbstractParam;

import java.util.*;
import java.util.regex.Pattern;

import static guru.nidi.ramltester.core.CheckerHelper.*;

/**
 *
 */
class RamlValidatorChecker {

    private static final String PARAM_CONDITION_ILLEGAL = "parameter.condition.illegal";

    enum ParamName {
        BASE_URI("baseUriParameter"),
        URI("uriParameter"),
        QUERY("queryParameter"),
        HEADER("header"),
        FORM("formParameter");
        private final String value;

        ParamName(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private final Raml raml;
    private final Locator locator;
    private final Pattern resourcePattern;
    private final Pattern parameterPattern;
    private final Pattern headerPattern;
    private final List<SchemaValidator> schemaValidators;
    private final EnumSet<Validation> validations;
    private final RamlReport report;
    private final RamlViolations violations;

    public RamlValidatorChecker(Raml raml, List<SchemaValidator> schemaValidators) {
        this(raml, new Locator(), schemaValidators, EnumSet.allOf(Validation.class), null, null, null);
    }

    public RamlValidatorChecker(Raml raml, Locator locator, List<SchemaValidator> schemaValidators, EnumSet<Validation> validations, Pattern resourcePattern, Pattern parameterPattern, Pattern headerPattern) {
        this.raml = raml;
        this.locator = locator;
        this.resourcePattern = resourcePattern;
        this.parameterPattern = parameterPattern;
        this.headerPattern = headerPattern;
        this.schemaValidators = schemaValidators;
        this.validations = validations;
        this.report = new RamlReport(raml);
        this.violations = report.getValidationViolations();
    }

    public RamlValidatorChecker withChecks(Validation... validations) {
        final EnumSet<Validation> validationSet = validations.length == 0 ? EnumSet.noneOf(Validation.class) : EnumSet.copyOf(Arrays.asList(validations));
        return new RamlValidatorChecker(raml, locator, schemaValidators, validationSet, headerPattern, parameterPattern, resourcePattern);
    }

    public RamlValidatorChecker withResourcePattern(String regex) {
        return new RamlValidatorChecker(raml, locator, schemaValidators, validations, regex == null ? null : Pattern.compile(regex), parameterPattern, headerPattern);
    }

    public RamlValidatorChecker withParameterPattern(String regex) {
        return new RamlValidatorChecker(raml, locator, schemaValidators, validations, resourcePattern, regex == null ? null : Pattern.compile(regex), headerPattern);
    }

    public RamlValidatorChecker withHeaderPattern(String regex) {
        return new RamlValidatorChecker(raml, locator, schemaValidators, validations, resourcePattern, parameterPattern, regex == null ? null : Pattern.compile(regex));
    }

    public Locator getLocator() {
        return locator;
    }

    public RamlReport getReport() {
        return report;
    }

    private boolean has(Validation validation) {
        return validations.contains(validation);
    }

    private void violation(String key, Object... params) {
        violations.add(new Message(key, params));
    }

    public void description(Map<String, ?> params, ParamName paramName) {
        if (has(Validation.DESCRIPTION)) {
            for (final Map.Entry<String, AbstractParam> param : paramEntries(params)) {
                description(param.getKey(), param.getValue(), paramName);
            }
        }
    }

    private void description(String name, AbstractParam param, ParamName paramName) {
        if (isNullOrEmpty(param.getDescription())) {
            violation("parameter.description.missing", locator, name, paramName);
        }
    }

    public void description(String desc) {
        if (has(Validation.DESCRIPTION)) {
            if (isNullOrEmpty(desc)) {
                violation("description.missing", locator);
            }
        }
    }

    public void description(List<DocumentationItem> docs) {
        if (has(Validation.DESCRIPTION)) {
            if (isNullOrEmpty(docs)) {
                violation("documentation.missing", locator);
            } else {
                for (final DocumentationItem doc : docs) {
                    if (isNullOrEmpty(doc.getTitle())) {
                        violation("documentation.missing.title", locator);
                    } else if (isNullOrEmpty(doc.getContent())) {
                        violation("documentation.missing.content", locator);
                    }
                }
            }
        }
    }

    public void empty(Resource resource) {
        if (has(Validation.EMPTY)) {
            if (resource.getActions().isEmpty() && resource.getResources().isEmpty()) {
                violation("empty", locator);
            }
        }
    }

    public void empty(Action action) {
        if (has(Validation.EMPTY)) {
            if (action.getResponses().isEmpty()) {
                violation("empty", locator);
            }
        }
    }

    private void baseUriParameters(Collection<String> names) {
        if (has(Validation.URI_PARAMETER)) {
            if (raml.getBaseUri() == null && !names.isEmpty()) {
                violation("baseUriParameters.illegal", locator);
            } else {
                for (final String name : names) {
                    if ("version".equals(name)) {
                        violation("baseUriParameter.illegal", locator, name);
                    } else if (!raml.getBaseUri().contains("{" + name + "}")) {
                        violation("baseUriParameter.invalid", locator, name);
                    }
                }
            }
        }
    }

    public void uriParameters(Collection<String> names, Resource resource) {
        if (has(Validation.URI_PARAMETER)) {
            for (final String name : names) {
                if ("version".equals(name)) {
                    violation("uriParameter.illegal", locator, name);
                } else if (!resource.getUri().contains("{" + name + "}")) {
                    violation("uriParameter.invalid", locator, name);
                }
            }
        }
    }

    public void resourcePattern(Resource resource) {
        if (resourcePattern != null) {
            final String uri = resource.getRelativeUri().replaceAll("\\{[^}/]+\\}", "");
            for (final String part : uri.split("/")) {
                if (part != null && part.length() > 0 && !resourcePattern.matcher(part).matches()) {
                    violation("resource.name.invalid", locator, resourcePattern.pattern());
                }
            }
        }
    }

    public void parameters(Map<String, ?> params, ParamName paramName) {
        if (paramName == ParamName.BASE_URI) {
            baseUriParameters(params.keySet());
        }
        if (parameterPattern != null) {
            for (final String name : params.keySet()) {
                if (!parameterPattern.matcher(name).matches()) {
                    violation("parameter.name.invalid", locator, name, paramName, parameterPattern.pattern());
                }
            }
        }
        if (has(Validation.PARAMETER)) {
            parameterDef(params, paramName);
        }
        if (has(Validation.EXAMPLE)) {
            final ParameterChecker checker = new ParameterChecker(violations);
            for (final Map.Entry<String, AbstractParam> param : paramEntries(params)) {
                parameterValues(param.getValue(), checker, new Message("parameter.condition", locator, param.getKey(), paramName));
            }
        }
    }

    public void formParameters(MimeType mimeType) {
        if (has(Validation.PARAMETER)) {
            if (!MediaType.valueOf(mimeType.getType()).isCompatibleWith(MediaType.FORM_URL_ENCODED) &&
                    !MediaType.valueOf(mimeType.getType()).isCompatibleWith(MediaType.MULTIPART)) {
                violation("formParameter.illegal", locator);
            }
        }
    }

    private void parameterDef(Map<String, ?> params, ParamName paramName) {
        for (final Map.Entry<String, AbstractParam> entry : paramEntries(params)) {
            final String name = entry.getKey();
            final AbstractParam param = entry.getValue();
            final ParamType type = param.getType() == null ? ParamType.STRING : param.getType();
            if (type == ParamType.STRING) {
                minMaxNotAllowed(param, name, paramName);
            } else {
                stringConstraintsNotAllowed(param, name, paramName);
                if (type != ParamType.INTEGER && type != ParamType.NUMBER) {
                    minMaxNotAllowed(param, name, paramName);
                }
                if (type == ParamType.FILE) {
                    violations.addIf(paramName != ParamName.FORM, new Message("parameter.file.illegal", locator, name, paramName));
                }
            }
        }
    }

    private void stringConstraintsNotAllowed(AbstractParam param, String name, ParamName paramName) {
        violations.addIf(param.getEnumeration() != null, new Message(PARAM_CONDITION_ILLEGAL, locator, name, paramName, "enum"));
        violations.addIf(param.getPattern() != null, new Message(PARAM_CONDITION_ILLEGAL, locator, name, paramName, "pattern"));
        violations.addIf(param.getMinLength() != null, new Message(PARAM_CONDITION_ILLEGAL, locator, name, paramName, "minLength"));
        violations.addIf(param.getMaxLength() != null, new Message(PARAM_CONDITION_ILLEGAL, locator, name, paramName, "maxLength"));
    }

    private void minMaxNotAllowed(AbstractParam param, String name, ParamName paramName) {
        violations.addIf(param.getMinimum() != null, new Message(PARAM_CONDITION_ILLEGAL, locator, name, paramName, "minimum"));
        violations.addIf(param.getMaximum() != null, new Message(PARAM_CONDITION_ILLEGAL, locator, name, paramName, "maximum"));
    }

    private void parameterValues(AbstractParam param, ParameterChecker checker, Message message) {
        if (param.getExample() != null) {
            checker.checkParameter(param, param.getExample(), message.withParam("example"));
        }
        if (param.getDefaultValue() != null) {
            checker.checkParameter(param, param.getDefaultValue(), message.withParam("default value"));
        }
    }

    public void headerPattern(Collection<String> names) {
        if (headerPattern != null) {
            for (final String name : names) {
                if (!headerPattern.matcher(name).matches()) {
                    violation("header.name.invalid", locator, name, headerPattern.pattern());
                }
            }
        }
    }

    public void exampleSchema(MimeType mimeType) {
        if (has(Validation.EXAMPLE)) {
            final String schema = mimeType.getSchema();
            final String example = mimeType.getExample();
            final SchemaValidator validator = findSchemaValidator(schemaValidators, MediaType.valueOf(mimeType.getType()));
            if (schema != null && example != null && validator != null) {
                validator.validate(new NamedReader(example, new Message("example").toString()), resolveSchema(raml, schema), violations,
                        new Message("schema.example.mismatch", locator, example));
            }
        }
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private boolean isNullOrEmpty(List<?> s) {
        return s == null || s.isEmpty();
    }
}
