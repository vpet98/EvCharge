import React from 'react';
import './FinishChargeVehicle.css';
import {user_roles} from '../app_essentials/App.js';
import {checkout} from '../api_comm/api.js';
import ActiveSessions from './ActiveSessions.js';
import Payment from './Payment.js';
import AppiErrorHandler from '../api_comm/error_handling.js';


export function getDate(){
  //date in yyyy-mm-dd hh:MM:ss format
  var date = new Date();

  //yyyy-mm-dd
  var yyyy = date.getFullYear();
  var mm = date.getMonth()+1;
  mm = mm < 10 ? '0'+mm : mm;
  var dd = date.getDate();
  dd = dd < 10 ? '0'+dd : dd;
  var d = yyyy+'-'+mm+'-'+dd;

  //hh:MM:ss
  var hh = date.getHours();
  hh = hh < 10 ? '0'+hh : hh;
  var min = date.getMinutes();
  min = min < 10 ? '0'+min : min;
  var ss = date.getSeconds();
  ss = ss < 10 ? '0'+ss : ss;
  var t = hh+':'+min+':'+ss;

  return d+' '+t;
}


class FinishChargeVehicle extends React.Component{
  constructor(props){
    super(props);
    this.state ={
      sessionId: "",
      cost: 0,
      status: "",
      error: null
    }
    this.handleInput = this.handleInput.bind(this);
    this.handleCheckout = this.handleCheckout.bind(this);
    this.handleRefresh = this.handleRefresh.bind(this);
  }

  handleInput(e){
    const name = e.target.name;
    const value = e.target.value;
    this.setState({ [name]: value });
  }

  handleCheckout(){
    let info = {
      sessionId: this.state.sessionId,
      time: getDate(),
      token: this.props.user.token
    }
    checkout(info)
      .then(json => {
        setTimeout(() => {
          console.log(json);
          if (!json.data.Response)
            this.setState({ status: "Ok" });
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

  handleRefresh(e){
    this.handleInput({target: {name: "status", value: ""}})
    this.handleInput({target: {name: "sessionId", value: ""}})
  }

  render(){
    let availableSessions = this.props.user !== null && this.props.user.hasOwnProperty('roles')
                            && this.props.user.roles !== [user_roles.guest];
    return(
      <>
      {!availableSessions &&(
        <h4>You must login to see active sessions.</h4>
      )}
      {availableSessions && this.state.status === "" &&(
      <div className="ActiveSessions">
        <ActiveSessions
          user={this.props.user}
          handleInput={this.handleInput}/>
        {this.state.sessionId !== "" &&(
          <div className="ChosenSession">
            <div className="details">
              <p> <p class="thick">Chosen session:</p> {this.state.sessionId} </p>
              <p> <p class="thick">Cost:</p> {this.state.cost} € </p>
            </div>
            <Payment
              user={this.props.user}
              state={this.state}
              handleCheckout={this.handleCheckout}/>
          </div>
        )}
        {this.state.error && (
          <div className="error"><p>{this.state.error}</p></div>
        )}
      </div>
      )}
      {availableSessions && this.state.status === "Ok" &&(
        <>
          <h4>Session successfully terminated!</h4>
          <button
            type="button"
            name="refresh"
            className="btn waves-effect waves-light"
            onClick={this.handleRefresh}
          > Continue here
          </button>
        </>
      )}
      </>
    );
  }
}

export default FinishChargeVehicle;
