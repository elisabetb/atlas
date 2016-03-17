"use strict";

//*------------------------------------------------------------------*

var React = require('react');
var ReactDOM = require('react-dom');

var $ = require('jquery');
require('jquery-ui-bundle');

//*------------------------------------------------------------------*



//*------------------------------------------------------------------*

var DisplayLevelsButton = React.createClass({

    propTypes: {
        hideText: React.PropTypes.string.isRequired,
        showText: React.PropTypes.string.isRequired,
        onClickCallback: React.PropTypes.func.isRequired,
        displayLevels: React.PropTypes.bool.isRequired,
        width: React.PropTypes.string
    },

    _buttonText: function () {
        return this.props.displayLevels ? this.props.hideText : this.props.showText;
    },

    _updateButtonText: function () {
        $(ReactDOM.findDOMNode(this)).button({ label: this._buttonText() });
    },

    render: function () {
        var style = this.props.width ? {textAlign: "center", width: this.props.width} : {textAlign: "center"};
        return (
            <button style={style} onClick={this.props.onClickCallback}/>
        );
    },

    componentDidMount: function () {
        this._updateButtonText();
    },

    componentDidUpdate: function () {
        this._updateButtonText();
    }

});

//*------------------------------------------------------------------*

module.exports = DisplayLevelsButton;