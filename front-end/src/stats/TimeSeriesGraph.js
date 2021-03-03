import React from 'react';
import './TimeSeriesGraph.css';
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";


// a react component to show time changing data
class TimeSeriesGraph extends React.Component{
  constructor(props){
    super(props);
    var today = new Date();
    var lastMonth = new Date();
    lastMonth.setMonth(lastMonth.getMonth() - 1);
    this.state = {
      show_graph: false,
      switch_checked: true,
      fDate: lastMonth,
      tDate: today,
      msg: "",
      error: ""
    };
    this.handleCheckboxInput = this.handleCheckboxInput.bind(this);
    this.handleFDateInput = this.handleFDateInput.bind(this);
    this.handleTDateInput = this.handleTDateInput.bind(this);
    this.makeGraph = this.makeGraph.bind(this);
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
          onClick={this.props.page_callback}
        >
          return
        </button>
        <form>
          <div className="switch">
            <label className="switch">
              charges
              <input type="checkbox" checked={this.state.switch_checked} onChange={this.handleCheckboxInput}/>
              <span className="lever"></span>
              Kw/h
            </label>
          </div>
          <label>From </label>
          <DatePicker
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
        </form>
        <button
          type="button"
          name="makeGraph"
          onClick={this.makeGraph}
        >
          Graph Stats
        </button>
        <p>{this.state.msg}</p>
        <p>{this.state.error}</p>
        <p>{this.props.title}</p>
        <p>{this.props.x_axis_title}</p>
        <p>{this.props.x_axis}</p>
        <p>{this.props.y_axis_title}</p>
        <p>{this.props.y_axis}</p>
        <p>{this.props.graph_aggregate}</p>
      </div>
    );
  }
}

export default TimeSeriesGraph;
