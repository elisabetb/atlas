"use strict";

//*------------------------------------------------------------------*

var React = require('react');
var ReactDOM = require('react-dom');

//*------------------------------------------------------------------*

var AtlasHeatmapBuilder = require('heatmap-anatomogram');

//*------------------------------------------------------------------*

var BaselineHeatmapWidget = React.createClass({
    propTypes: {
        atlasHost: React.PropTypes.string.isRequired,
        geneQuery: React.PropTypes.string.isRequired,
        species: React.PropTypes.string.isRequired,
        factor: React.PropTypes.string.isRequired,
        showAnatomogram: React.PropTypes.bool.isRequired,
        showHeatmapLabel: React.PropTypes.bool.isRequired,
    },

    componentDidMount: function() {
        AtlasHeatmapBuilder({
            atlasHost: this.props.atlasHost,
            params: "geneQuery=" + this.props.geneQuery + "&species=" + this.props.species + "&source=" + this.props.factor,
            analyticsSearch: true,
            isMultiExperiment: true,
            target: ReactDOM.findDOMNode(this.refs.widgetBody),
            isWidget: false,
            showAnatomogram: this.props.showAnatomogram
        });
    },

    componentDidUpdate: function() {
        AtlasHeatmapBuilder({
            atlasHost: this.props.atlasHost,
            params: "geneQuery=" + this.props.geneQuery + "&species=" + this.props.species + "&source=" + this.props.factor,
            analyticsSearch: true,
            isMultiExperiment: true,
            target: ReactDOM.findDOMNode(this.refs.widgetBody),
            isWidget: false,
            showAnatomogram: this.props.showAnatomogram
        });
    },

    render: function() {
        var speciesLabel = this._capitalize(this.props.species);
        var factorLabel = this._capitalize(this._removeUnderScore(this.props.factor));

        var widgetTitle = <h5>{(this.props.showHeatmapLabel ? speciesLabel + " — " : "") + factorLabel}</h5>;

        return(
            <div className="gxaBaselineHeatmap">
                {widgetTitle}
                <div ref="widgetBody" style={{paddingBottom: "30px"}}></div>
            </div>
        );
    },

    _capitalize: function capitalizeFirstLetter(str) {
        return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    },

    _removeUnderScore: function removeUnderScoreForWhiteSpace(str) {
        return str.replace(/[-_.]/g, ' ');
    }

});

//*------------------------------------------------------------------*

module.exports = BaselineHeatmapWidget;
