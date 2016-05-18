/*
 * Copyright 2008-2016 Gene Expression Team, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Expression Atlas:
 * https://www.ebi.ac.uk/gxa
 *
 * For further details of the Expression Atlas project, including source code,
 * downloads and documentation, please see:
 * https://github.com/gxa/atlas
 */

/**
 * Created by Alfonso Muñoz-Pomer Fuentes <amunoz@ebi.ac.uk> on 04/03/2016.
 */

package uk.ac.ebi.atlas.experimentimport.coexpression;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class BaselineCoexpression implements Comparable<BaselineCoexpression> {
    static BaselineCoexpression create( double ceStatistic, String ceGeneID) {
        return new AutoValue_BaselineCoexpression(ceStatistic, ceGeneID);
    }

    public abstract double ceStatistic();
    public abstract String ceGeneID();

    @Override
    public int compareTo(BaselineCoexpression other) {

        int c= Double.compare(this.ceStatistic(), other.ceStatistic());

        return c == 0
                ? this.ceGeneID().compareTo(other.ceGeneID())
                : c;
    }
}
