import React from 'react';
import './TimeSeriesGraph.css';

// a react component to show time changing data
class TimeSeriesGraph extends React.Component{
  constructor(props){
    super(props);
    var today = new Date();
    var lastMonth = new Date();
    lastMonth.setMonth(lastMonth.getMonth - 1);
    this.state = {
      show_graph: false,
      switch_checked: false,
      fDate: today,
      tDate: lastMonth
    };
    this.onInputChange = this.onInputChange.bind(this);
  }

  // a function to handle the changes of the values of the elements
  onInputChange(e){
    if(e.target.type === "checkbox"){
      const v = e.target.checked;
      this.setState({ switch_checked: v});
    }else{
      if(e.target.name === "fDate"){
        const v = e.target.value;
        this.setState({ fDate: new Date(v) });
      }
      else if(e.target.name === "tDate"){
        const v = e.target.value;
        this.setState({ tDate: new Date(v) });
      }
    }
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
              <input type="checkbox" onChange={this.onInputChange}/>
              <span className="lever"></span>
              Kw/h
            </label>
          </div>
          <label>From</label>
          <input type="date" name="fDate" className="datepicker" onChange={this.onInputChange}/>
          <label>To</label>
          <input type="date" name="tDate" className="datepicker" onChange={this.onInputChange}/>
        </form>
        <p>{this.props.title}</p>
      </div>
    );
  }
}

export default TimeSeriesGraph;
