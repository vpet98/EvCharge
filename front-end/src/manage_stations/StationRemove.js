import React from 'react';
import './StationRemove.css';
import {removeStation} from '../api_comm/api.js';
import AppiErrorHandler from '../api_comm/error_handling.js';

class StationRemove extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      stationId: "",
      insert_mode: false,
      status: "",
      error: null
    };
    this.handleInput = this.handleInput.bind(this);
    this.handleRemove = this.handleRemove.bind(this);
    this.removeStations = this.removeStations.bind(this);
  }

  handleInput(e){
    const name = e.target.name;
    const value = e.target.value;
    this.setState({ [name]: value });
  }

  handleRemove(e){
    const value = e.target.name === "close" ? false : true;
    this.setState({insert_mode: value});
    this.setState({status: ""});
    this.setState({error: null});
  }

  removeStations(){
    let obj = {
      station: this.state.stationId,
      token: this.props.user.token
    };
    removeStation(obj)
      .then(json => {
        setTimeout(() => {
          console.log(json);
          if (!json.data.Response){
            this.setState({ status: "Ok" });
            this.props.handleInput();
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

  render(){
    return(
      <div className="RemoveStation">
        {!this.state.insert_mode && (
          <button
            type="button"
            name="RemoveStation"
            className="btn waves-effect waves-light remove_btn"
            onClick={this.handleRemove}
            > -
          </button>
        )}
        {this.state.insert_mode &&(
          <div className="RemoveProcess">
            <h5>Insert Station Id</h5>
            <input
              type="text"
              name="stationId"
              field="StationId"
              placeholder="station id"
              value={this.state.stationId}
              onChange={this.handleInput}
            />
            <button
              type="button"
              name="Remove"
              className="btn waves-effect waves-light remove_btn"
              onClick={this.removeStations}
            > Remove
            </button>
            <button
              type="button"
              name="close"
              className="btn waves-effect waves-light close_btn"
              onClick={this.handleRemove}
            > Close
            </button>
            {this.state.status &&(
              <p>Station removed successfully!</p>
            )}
          </div>
        )}
        {this.state.error && (
          <div className="error"><p>{this.state.error}</p></div>
        )}
      </div>
    );
  }
}

export default StationRemove;
