import React from 'react';
import './ChargeVehicle.css';
import {pages, user_roles} from '../app_essentials/App.js';
import {getSessionCost, startSession} from '../api_comm/api.js';
import Charge from './Charge.js';
import AppiErrorHandler from '../api_comm/error_handling.js';

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
    this.handleInput = this.handleInput.bind(this);
    this.handleEnterInfo = this.handleEnterInfo.bind(this);
    this.startcharging = this.startcharging.bind(this);
    this.gotoPayingPage = this.gotoPayingPage.bind(this);
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
        let handler = new AppiErrorHandler(err);
        let txt = handler.getMessage();
        if(txt !== null){
          if(handler.getError() !== null) txt = txt + '\n' + handler.getError();
        }else txt = handler.getError();
        this.setState({ error: txt });
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
        let handler = new AppiErrorHandler(err);
        let txt = handler.getMessage();
        if(txt !== null){
          if(handler.getError() !== null) txt = txt + '\n' + handler.getError();
        }else txt = handler.getError();
        this.setState({ error: txt });
      });
  }


  render(){
    let enableCharging = this.props.user !== null && this.props.user.hasOwnProperty('roles')
                            && this.props.user.roles !== [user_roles.guest];
    return(
      <div className="ChargingStates">
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
        )}
       {this.state.page === charging_pages.SuccessfulCharging &&(
         <div className="SuccessfulChargingPage">
          <h4>Charging has successfully begun!</h4>
            <div className="details">
              <h5>Charging info:</h5>
              <p> <p class="thick">Vehicle:</p> {this.state.vehicle} </p>
              <p> <p class="thick">Expected Cost:</p> {Number(this.state.cost).toFixed(2)} € </p>
            </div>
            <h5>Now finish your charging and pay here.</h5>
            <button
              type="button"
              name="pay"
              className="btn waves-effect waves-light"
              onClick={this.gotoPayingPage}
            > Pay
            </button>
         </div>
       )}
       </div>
    );
  }
}

export default ChargeVehicle;
