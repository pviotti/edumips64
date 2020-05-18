import React from "react";

import MonacoEditor from 'react-monaco-editor';

// Number of steps to run with the multi-step button.
const STEP_STRIDE = 500;

const MonacoCode = (props) => {
    const options = {
        selectOnLineNumbers: true,
        roundedSelection: false,
        readOnly: false,
        cursorStyle: "line",
        automaticLayout: false,
        codelens: false,
        minimap: {enabled: false},
    };

    const onChange = (newValue, event) => {
        props.onChangeValue(newValue);
    }

    return (
        <MonacoEditor
            height="400"
            language="mips"
            value={props.code}
            options={options}
            onChange={onChange}
            theme="vs-light"
        />
    )
}

const Code = (props) => {
    return (
        <div id="code">
            <MonacoCode 
                code={props.code}
                onChangeValue={props.onChangeValue}
                />
            <div id="controls">
                <input id="load-button" type="button" value="Load/Reset" onClick={() => {props.onLoadClick()}} disabled={!props.loadEnabled} />
                <input id="step-button" type="button" value="Single Step" onClick={() => {props.onStepClick(1)}} disabled={!props.stepEnabled} />
                <input id="multi-step-button" type="button" value="Multi Step" onClick={() => {props.onStepClick(STEP_STRIDE)}} disabled={!props.stepEnabled} />
                <input id="run-button" type="button" value="Run All" onClick={() => {props.onRunClick()}} disabled={!props.runEnabled} />
                <input id="stop-button" type="button" value="Stop" onClick={() => {props.onStopClick()}} disabled={!props.stopEnabled} />
            </div>
        </div>
    );
}

export default Code;