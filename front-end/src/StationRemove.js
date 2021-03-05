import React from 'react';
import './StationRemove.css';
import {removeStation} from './api.js';

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
        this.setState({ error: err.response.data.message });
      });
  }

  render(){
    return(
      <div className="RemoveStation">
        <button
          type="button"
          name="RemoveStation"
          onClick={this.handleRemove}
          > -
        </button>
        {this.state.insert_mode &&(
          <div className="RemoveProcess">
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
              onClick={this.removeStations}
            > Remove
            </button>
            <button
              type="button"
              name="close"
              onClick={this.handleRemove}
            > Close
            </button>
            {this.state.status &&(
              <p>Station removed successfully!</p>
            )}
          </div>
        )}
        {this.state.error &&(
          <p>{this.state.error}</p>
        )}
      </div>
    );
  }
}

export default StationRemove;
