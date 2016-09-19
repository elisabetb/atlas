package uk.ac.ebi.atlas.search;

import com.google.common.base.Throwables;
import com.google.gson.stream.MalformedJsonException;

import java.beans.PropertyEditorSupport;
import java.io.UnsupportedEncodingException;

public class SemanticQueryPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text)  {
        try {
            setValue(SemanticQuery.fromUrlEncodedJson(text));
        } catch (UnsupportedEncodingException | MalformedJsonException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getAsText() {
        return ((SemanticQuery) this.getValue()).toUrlEncodedJson();
    }

}
