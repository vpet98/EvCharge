import React from 'react';
import './StationsManage.css';
import {user_roles} from './App.js';
import {showStation} from './api.js';
import StationShow from './StationShow.js'
import StationAdd from './StationAdd.js'
import StationRemove from './StationRemove.js'

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
    let obj = {
      operator: this.props.user.username,
      token: this.props.user.token
    }
    showStation(obj)
      .then(json => {
        setTimeout(() => {
          console.log(json);
          if (!json.data.Response){
            this.setState({ stations: json.data.StationIDsList });
          }
          else
            this.setState({ error: json.response.data.message });
        }, 0)
      })
      .catch(err =>{
        this.setState({ error: err.response.data.message });
      });
  }


  render(){
    let isOperator = this.props.user !== null && this.props.user.hasOwnProperty('role')
                            && this.props.user.role === user_roles.operator;
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
