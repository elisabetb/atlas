"use strict";

//*------------------------------------------------------------------*

var React = require('react');

var Modal = require('react-bootstrap/lib/Modal');
var Button = require('react-bootstrap/lib/Button');
var Glyphicon = require('react-bootstrap/lib/Glyphicon');
var Tooltip = require('react-bootstrap/lib/Tooltip');
var OverlayTrigger = require('react-bootstrap/lib/OverlayTrigger');

var BlueprintText = require('./BlueprintText.jsx');

//*------------------------------------------------------------------*



//*------------------------------------------------------------------*
var DownloadProfilesButton = React.createClass({
    propTypes: {
        atlasBaseURL: React.PropTypes.string.isRequired,
        downloadProfilesURL: React.PropTypes.string.isRequired,
        isFortLauderdale: React.PropTypes.bool.isRequired,
        onDownloadCallbackForAnalytics: React.PropTypes.func.isRequired
    },
    getInitialState: function() {
        return { showModal: false };
    },

    _closeModal: function() {
        this.setState({ showModal: false });
    },

    _afterDownloadButtonClicked: function() {
        if(!this.props.isFortLauderdale) {
            this._commenceDownload();
        } else {
            this.setState({ showModal: true });
        }
    },

    _commenceDownload: function() {
        this.props.onDownloadCallbackForAnalytics();
        window.location.href=this.props.atlasBaseURL + this.props.downloadProfilesURL;
    },

    _commenceDownloadAndCloseModal: function() {
        this._commenceDownload();
        this._closeModal();
    },

    render: function() {
        return (
            <a ref="downloadProfilesLink" onClick={this._afterDownloadButtonClicked}>
                <OverlayTrigger
                    placement="bottom"
                    overlay={<Tooltip id="downloadResultsTooltip">Download all results</Tooltip>}
                    delay={0}>
                    <Button bsStyle="primary" bsSize="xsmall"><Glyphicon glyph="download-alt"/></Button>
                </OverlayTrigger>

                <Modal show={this.state.showModal} onHide={this._closeModal} bsSize="large">
                    <Modal.Header closeButton>
                    </Modal.Header>
                    <Modal.Body style={{maxHeight: '360px'}}>
                        <BlueprintText/>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this._closeModal}>Close</Button>
                        <Button bsStyle="primary" onClick={this._commenceDownloadAndCloseModal}>Continue downloading</Button>
                    </Modal.Footer>
                </Modal>
            </a>
        );
    }
});

module.exports = DownloadProfilesButton;
