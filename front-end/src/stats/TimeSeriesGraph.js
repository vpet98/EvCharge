import React from 'react';
import './TimeSeriesGraph.css';
import DatePicker from "react-datepicker";
import CanvasJSReact from './assets/canvasjs.react';
import "react-datepicker/dist/react-datepicker.css";


// a react component to show time changing data
class TimeSeriesGraph extends React.Component{
  constructor(props){
    super(props);
    var today = new Date();
    var lastMonth = new Date();
    lastMonth.setMonth(lastMonth.getMonth() - 1);
    this.state = {
      switch_checked: true,
      fDate: lastMonth,
      tDate: today,
    };
    this.handleCheckboxInput = this.handleCheckboxInput.bind(this);
    this.handleFDateInput = this.handleFDateInput.bind(this);
    this.handleTDateInput = this.handleTDateInput.bind(this);
    this.makeGraph = this.makeGraph.bind(this);
  }

  // make graph as soon as the component render
  componentDidMount(){
    // this.makeGraph();
  }

  // a function to handle the changes of the values of fDate input
  handleFDateInput(date){
    this.setState({ fDate: date });
  }

  // a function to handle the changes of the values of tDate input
  handleTDateInput(date){
    this.setState({ tDate: date });
  }

  // a function to handle the changes of the values of checkbox
  handleCheckboxInput(e){
      this.setState({ switch_checked: e.target.checked });
  }

  makeGraph(){
    this.props.data_callback({
      from_date: this.state.fDate,
      to_date: this.state.tDate,
      graph_kw: this.state.switch_checked
    });
  }

  render(){
    return(
      <div className="graph">
        <button
          type="button"
          name="return"
          className="btn waves-effect waves-light"
          onClick={this.props.page_callback}
        >
          return
        </button>
        <form>
          <div className="switch">
            <label className="switch">
              {this.props.secondDataName}
              <input type="checkbox" checked={this.state.switch_checked} onChange={this.handleCheckboxInput}/>
              <span className="lever"></span>
              Kw/h
            </label>
          </div>
          <div style={{display: 'flex', flexDirection: 'row'}}>
            <label>From </label>
            <DatePicker
              popperProps={{ positionFixed: true }}
              type="date"
              name="fDate"
              dateFormat="dd MMM yyyy"
              selected={this.state.fDate}
              className="datepicker"
              onChange={this.handleFDateInput}
            />
            <label>To </label>
            <DatePicker
              type="date"
              name="tDate"
              dateFormat="dd MMM yyyy"
              selected={this.state.tDate}
              className="datepicker"
              onChange={this.handleTDateInput}
            />
            <button
              type="button"
              name="makeGraph"
              className="btn waves-effect waves-light"
              onClick={this.makeGraph}
            >
              Graph Stats
            </button>
          </div>
        </form>
        <p>{this.props.msg}</p>
        {this.props.error !== null && (
          <div className="error"><p>{this.props.error}</p></div>
        )}
        {this.props.graph_options && (
          <>
            <p>Graph integral: {this.props.graph_options.graph_aggregate}</p>
            <Graph graph_options={this.props.graph_options}/>
          </>
        )}
      </div>
    );
  }
}

var CanvasJSChart = CanvasJSReact.CanvasJSChart;
class Graph extends React.Component{
  mergeData(x_axis, y_axis){
    return x_axis.map((e, i) => ({ label: e, y: y_axis[i] }))
  }

  render(){
    const options = {
  			title: { text: this.props.graph_options.graph_title },
        axisX: { title: this.props.graph_options.x_axis_title },
        axisY: { title: this.props.graph_options.y_axis_title },
  			data: [
    			{
    				type: "column",
    				dataPoints: this.mergeData(this.props.graph_options.x_axis, this.props.graph_options.y_axis)
    			}
  			]
  		};
      return (
  		<div>
  			<CanvasJSChart options = {options} />
  		</div>
		);
  }
}

export default TimeSeriesGraph;
