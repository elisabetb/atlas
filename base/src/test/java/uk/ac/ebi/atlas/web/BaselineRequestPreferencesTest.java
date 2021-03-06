package uk.ac.ebi.atlas.web;

import uk.ac.ebi.atlas.model.ExpressionUnit;

public class BaselineRequestPreferencesTest {

    public static BaselineRequestPreferences get(){
        return new BaselineRequestPreferences() {
            @Override
            public double getDefaultCutoff() {
                return 0.0;
            }

            public ExpressionUnit.Absolute getUnit(){
                return ExpressionUnit.Absolute.Rna.TPM;
            }
        };
    }

}