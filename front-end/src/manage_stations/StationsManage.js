import React from 'react';
import './StationsManage.css';
import {user_roles} from '../app_essentials/App.js';
import {showStation} from '../api_comm/api.js';
import StationShow from './StationShow.js'
import StationAdd from './StationAdd.js'
import StationRemove from './StationRemove.js'
import AppiErrorHandler from '../api_comm/error_handling.js';

class StationsManage extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      stations: [],
      error: null
    }
    this.getStations = this.getStations.bind(this);
    this.handleInput = this.handleInput.bind(this);
    //this.getStations();
  }

  componentDidMount(){
    this.getStations();
  }

  handleInput(){
    this.getStations();
  }

  getStations(){
    if(this.props.user !== null){
      let obj = {
        operator: this.props.user.username,
        token: this.props.user.token
      }
      showStation(obj)
        .then(json => {
          setTimeout(() => {
            console.log(json);
            if (!json.data.Response){
              this.setState({ stations: json.data.StationsList });
            }
            else
              this.setState({ error: json.response.data.message });
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
      else{
        this.setState({ error: "error" });
      }
  }


  render(){
    let isOperator = this.props.user !== null && this.props.user.hasOwnProperty('roles')
                            && this.props.user.roles.includes(user_roles.operator);
    return(
      <div className="StaionsManage">
        {isOperator &&(
          <div className="StationsFunctions">
            <StationShow state={this.state}/>
            <StationAdd user={this.props.user} handleInput={this.handleInput}/>
            <StationRemove user={this.props.user} handleInput={this.handleInput}/>
          </div>
        )}
        {!isOperator &&(
          <h4>You have no stations to manage. You must be an operator.</h4>
        )}
      </div>
    );
  }
}

export default StationsManage;
