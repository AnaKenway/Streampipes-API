## Trend with Numerical Comparison (AI Regio)

<p align="center"> 
    <img src="icon.png" width="150px;" class="pe-image-documentation"/>
</p>

***

## Description

Detects a trend based on numerical comparison in incoming events using Siddhi engine to perform Complex Event
Processing.

***

## Required input

This pipeline element works with any input event that has one field containing a numerical value.

***

## Configuration

##### Property

Property which trend is being detected.

##### Window length

Length of the trend being detected.

##### Comparison operator

Comparison operator that is used in trend detection.

##### Comparison value

Comparison value that is used in trend detection.

##### Output property name

Name of the output property which values are either "True" or "False".

## Output

This pipeline element outputs "True" if it detects a trend, otherwise it outputs "False".
