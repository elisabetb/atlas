import React from 'react';
import URI from 'urijs';
const EbiSpeciesIcon = require('react-ebi-species').Icon;

const SpeciesItem = props =>
{
    const allExperimentsUrl = URI(props.atlasUrl).segment(`experiments`).addSearch({ organism: props.species });
    const differentialExperimentsUrl = URI(allExperimentsUrl).addSearch({ experimentType: `differential` });
    const baselineExperimentsUrl = URI(allExperimentsUrl).addSearch({ experimentType: `baseline` });

    const speciesFirstCapitalLetter = props.species[0].toUpperCase() + props.species.substr(1);

    return (
        <div className={"columns small-4 text-center combo" + (props.last ? " end" : "")}>
            <a href={allExperimentsUrl}>
                <span className="large-species-icon">
                    <EbiSpeciesIcon species={props.species}/>
                </span>

                <h5 className="species">{speciesFirstCapitalLetter}</h5>
            </a>

            <p className="experiments">
                {props.totalExperiments} experiments<br/>
                <a href={differentialExperimentsUrl} className="differential">
                    <span data-tooltip aria-haspopup="true" className="differential tiny button-rd" title="Differential experiments">D</span>
                    {props.baselineExperiments}
                </a>
                <a href={baselineExperimentsUrl} className="baseline padding-left-medium">
                    <span data-tooltip aria-haspopup="true" className="baseline tiny button-rd" title="Baseline experiments">B</span>
                    {props.differentialExperiments}
                </a>
            </p>
        </div>
    );
};

SpeciesItem.propTypes = {
    atlasUrl: React.PropTypes.string.isRequired,
    species: React.PropTypes.string.isRequired,
    totalExperiments: React.PropTypes.number.isRequired,
    baselineExperiments: React.PropTypes.number.isRequired,
    differentialExperiments: React.PropTypes.number.isRequired,
    last: React.PropTypes.bool.isRequired
};

export default SpeciesItem;