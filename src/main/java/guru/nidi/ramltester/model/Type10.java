package guru.nidi.ramltester.model;

import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Type10 implements UnifiedType {
    private TypeDeclaration type;

    public Type10(TypeDeclaration type) {
        this.type = type;
    }

    static List<UnifiedType> of(List<TypeDeclaration> types) {
        final List<UnifiedType> res = new ArrayList<>();
        for (final TypeDeclaration td : types) {
            res.add(new Type10(td));
        }
        return res;
    }

    @Override
    public Object delegate() {
        return type;
    }

    @Override
    public String name() {
        return type.name();
    }

    @Override
    public String description() {
        return type.description().value();
    }

    @Override
    public List<String> examples() {
        final List<String> res = new ArrayList<>();
        for (final ExampleSpec ex : type.examples()) {
            res.add(ex.value());
        }
        return res;
    }

    @Override
    public String defaultValue() {
        return type.defaultValue();
    }
}