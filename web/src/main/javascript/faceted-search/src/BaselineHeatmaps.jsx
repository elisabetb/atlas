"use strict";

//*------------------------------------------------------------------*

var React = require('react');

//*------------------------------------------------------------------*

var BaselineHeatmapWidget = require('./BaselineHeatmapWidget.jsx');

//*------------------------------------------------------------------*

var BaselineHeatmaps = React.createClass({
    propTypes: {
        geneQuery: React.PropTypes.string.isRequired,
        atlasHost: React.PropTypes.string.isRequired,
        /*
         [{"geneQuery":"GO:0001234","species":"Homo sapiens","factor":"CELL_LINE"},
          {"geneQuery":"GO:0001234","species":"Mus musculus","factor":"ORGANISM_PART"}]
         */
        showAnatomograms: React.PropTypes.bool.isRequired,
        heatmaps: React.PropTypes.arrayOf(React.PropTypes.shape({
            geneQuery: React.PropTypes.string.isRequired,
            species: React.PropTypes.string.isRequired,
            factor: React.PropTypes.string.isRequired
        })).isRequired
    },

    _isOrganismPart: function(a){
        return a.factor==="ORGANISM_PART";
    },
    _isNotOrganismPart: function(a){
        return ! this._isOrganismPart(a);
    },

    render: function () {
        var heatmapsInOrderWeWant=this.props.heatmaps.filter(this._isOrganismPart).concat(this.props.heatmaps.filter(this._isNotOrganismPart));
        return (
            <div>
                {heatmapsInOrderWeWant.map(function (heatmap) {
                    return <BaselineHeatmapWidget key={heatmap.species + "_" + heatmap.factor}
                                                  showAnatomogram={this.props.showAnatomograms}
                                                  showHeatmapLabel={this._hasMoreThanOneSpecies()} species={heatmap.species} factor={heatmap.factor}
                                                  atlasHost={this.props.atlasHost} geneQuery={this.props.geneQuery} />;
                }.bind(this))}
            </div>
        );
    },

    _hasMoreThanOneSpecies: function() {
        var species = [];
        for (var i = 0 ; i < this.props.heatmaps.length ; i++) {
            if (species.indexOf(this.props.heatmaps[i].species) === -1) {
                species.push(this.props.heatmaps[i].species);
            }
        }
        return species.length > 1;
    }
});

//*------------------------------------------------------------------*

module.exports = BaselineHeatmaps;

