import React from 'react';
import './ChargeVehicle.css';
import {pages, user_roles} from './App.js';
import {getSessionCost, startSession} from './api.js';

const charging_pages = {
  main: "main",
  SuccessfulCharging: "SuccessfulCharging"
};

class ChargeVehicle extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      vehicle: "",
      station: "",
      point: "",
      protocol_cost: null,
      cost: "",
      amount: "",
      error: null,
      page: charging_pages.main
    };
    this.handleHome = this.handleHome.bind(this);
    this.handleInput = this.handleInput.bind(this);
    this.handleEnterInfo = this.handleEnterInfo.bind(this);
    this.startcharging = this.startcharging.bind(this);
    this.gotoPayingPage = this.gotoPayingPage.bind(this);
  }

  // handle the return button of the form to return to the main page without loging in
  handleHome(e){
    this.props.callback({
      page: pages.main
    });
  }

  gotoPayingPage(){
    this.props.callback({
      page: pages.finish_charge
    });
  }

  // handle any change in input areas
  handleInput(e){
    const name = e.target.name;
    const value = e.target.value;
    this.setState({ [name]: value });
    this.setState({error: null});
  }

  handleEnterInfo(e){
    if (this.state.vehicle === "" || this.state.station === "" || this.state.point === ""){
      this.setState({ error: "Not enough parameters given." });
      return;
    }
    let info = {
      vehicle: this.state.vehicle,
      station_point: this.state.station+'_'+this.state.point,
      token: this.props.user.token
    }
    getSessionCost(info)
      .then(json => {
        setTimeout(() => {
          console.log(json);
          if (!json.data.Response)
            this.setState({ protocol_cost: json.data });
          else
            this.setState({ error: json.data.Response });
        }, 0)
      })
      .catch(err =>{
        this.setState({ error: err.response.data.message });
      });
  }

  startcharging(e){
    let info = {
      vehicle: this.state.vehicle,
      station_point: this.state.station+'_'+this.state.point,
      cost: this.state.cost,
      amount: this.state.amount,
      token: this.props.user.token
    };
    startSession(info)
      .then(json => {
        setTimeout(() => {
          console.log(json);
          if (json.data.Response)
            this.setState({ error: json.data.Response });
          else{
            let expected_cost =
              this.state.protocol_cost.cost>0 && info.cost ? info.cost
                : info.amount*this.state.protocol_cost.cost;
            this.setState({ cost: expected_cost });
            this.setState({ page: charging_pages.SuccessfulCharging });
          }
        }, 0)
      })
      .catch(err =>{
        this.setState({ error: err.response.data.message });
      });
  }


  render(){
    let enableCharging = this.props.user !== null && this.props.user.hasOwnProperty('role')
                            && this.props.user.role !== user_roles.guest;
    return(
      <div className="ChargingStates">
        <button
          type="button"
          name="home"
          onClick={this.handleHome}
          > Home
        </button>
        {this.state.page === charging_pages.main &&(
          <div className="MainPage">
            {!enableCharging && (
              <h4>You must login to charge your vehicle.</h4>
            )}
            {enableCharging &&(
              <Charge state={this.state} handleInput={this.handleInput}
                        handleEnterInfo={this.handleEnterInfo} startcharging={this.startcharging}/>
            )}
          </div>
        )
       }
       {this.state.page === charging_pages.SuccessfulCharging &&(
         <div className="SuccessfulChargingPage">
          <h4>Charging has successfully begun!</h4>
            <p>Charging info:</p>
              <div> Vehicle: {this.state.vehicle}</div>
              <div> Expected Cost: {this.state.cost}</div>
            <p>Now finish your charging and pay here.</p>
            <button
              type="button"
              name="pay"
              onClick={this.gotoPayingPage}
            > Pay
            </button>
         </div>
       )}
       </div>
    );
  }
}

class Charge extends React.Component{

  render(){
    return(
      <div className="begin_charging_page">
        <h4>Start Charging</h4>
          <GetInfo state={this.props.state} handleInput={this.props.handleInput}
                      handleEnterInfo={this.props.handleEnterInfo}/>
          {this.props.state.error &&(
            <p>{this.props.state.error}</p>
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

export default ChargeVehicle;
