import React from 'react';
import './Charge.css';

class Charge extends React.Component{

  render(){
    return(
      <div>
      <div className="begin_charging_page">
        <h1>Start Charging</h1>
          <GetInfo state={this.props.state} handleInput={this.props.handleInput}
                      handleEnterInfo={this.props.handleEnterInfo}/>
          {this.props.state.error &&(
            <div className="error"><p>{this.props.state.error}</p></div>
          )}
      </div>
      {!this.props.state.error && this.props.state.protocol_cost &&(
          <div className="charging_avaiable">
              <div className="chargeDetails">
                <p> <p class="thick">Protocol:</p> {this.props.state.protocol_cost.protocol}</p>
                <p> <p class="thick">Cost:</p> {this.props.state.protocol_cost.cost}</p>
               </div>
              <ChooseCostOrAmount state={this.props.state} handleInput={this.props.handleInput}
                                                        startcharging={this.props.startcharging}/>
            </div>
      )}
      </div>
    );
  }
}

class GetInfo extends React.Component{
  constructor(props){
    super(props);
    this.handleInput = this.handleInput.bind(this);
  }

  handleInput(e){
    this.props.handleInput(e);
    this.props.handleInput({target: {name: 'protocol_cost', value: null}});
  }

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
              onChange={this.handleInput}
            />
          <p>Station</p>
            <input
              type="text"
              name="station"
              field="station"
              placeholder="station"
              value={this.props.state.station}
              onChange={this.handleInput}
            />
          <p>Station Point</p>
            <input
              type="text"
              name="point"
              field="point"
              placeholder="point"
              value={this.props.state.point}
              onChange={this.handleInput}
            />
        <button
          type="button"
          name="enter_info"
          className="btn waves-effect waves-light"
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
        <h5>Choose charging cost or energy.</h5>
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
          <button
            type="button"
            name="start_charging"
            className="btn waves-effect waves-light"
            onClick={this.props.startcharging}
          > Start Charging!
          </button>
        )}
      </div>
      );
    }
}

export default Charge;
