import React from 'react';
import './Charge.css';

class Charge extends React.Component{

  render(){
    return(
      <div className="begin_charging_page">
        <h4>Start Charging</h4>
          <GetInfo state={this.props.state} handleInput={this.props.handleInput}
                      handleEnterInfo={this.props.handleEnterInfo}/>
          {this.props.state.error &&(
            <div className="error"><p>{this.props.state.error}</p></div>
          )}
          {!this.props.state.error && this.props.state.protocol_cost &&(
            <div className="charging_avaiable">
              <div> Protocol: {this.props.state.protocol_cost.protocol}</div>
              <div> Cost: {this.props.state.protocol_cost.cost}</div>
              <ChooseCostOrAmount state={this.props.state} handleInput={this.props.handleInput}
                                                        startcharging={this.props.startcharging}/>
            </div>
          )}
        </div>
    );
  }
}

class GetInfo extends React.Component{

  render() {
    return(
      <div className="enter_charging_data_div">
        <h5>Check compatibility!</h5>
          <p>Your Vehicle</p>
            <input
              type="text"
              name="vehicle"
              field="vehicle"
              placeholder="vehicle"
              value={this.props.state.vehicle}
              onChange={this.props.handleInput}
            />
          <p>Station</p>
            <input
              type="text"
              name="station"
              field="station"
              placeholder="station"
              value={this.props.state.station}
              onChange={this.props.handleInput}
            />
          <p>Station Point</p>
            <input
              type="text"
              name="point"
              field="point"
              placeholder="point"
              value={this.props.state.point}
              onChange={this.props.handleInput}
            />
        <button
          type="button"
          name="enter_info"
          onClick={this.props.handleEnterInfo}
        > Check
        </button>
      </div>
    )}
}


class ChooseCostOrAmount extends React.Component{

  render(){
    return(
      <div className="choose_cost_or_amount">
        <h4>Choose charging cost or energy.</h4>
        <p>Cost</p>
        <input
          type="text"
          name="cost"
          field="cost"
          placeholder="cost"
          value={this.props.state.cost}
          onChange={this.props.handleInput}
        />
        <p>Energy</p>
        <input
          type="text"
          name="amount"
          field="amount"
          placeholder="amount"
          value={this.props.state.amount}
          onChange={this.props.handleInput}
        />
        {(this.props.state.cost !== "" || this.props.state.amount !== "") &&(
          <>
          <button
            type="button"
            name="start_charging"
            onClick={this.props.startcharging}
          > Start Charging!
          </button>
          </>
        )}
      </div>
      );
    }
}

export default Charge;
