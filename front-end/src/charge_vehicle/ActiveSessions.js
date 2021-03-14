import React from 'react';
import './ActiveSessions.css';
import {getSessions} from '../api_comm/api.js';
import AppiErrorHandler from '../api_comm/error_handling.js';

class ActiveSessions extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      active_sessions : []
    };
    this.getsessions = this.getsessions.bind(this);
    this.chooseSession = this.chooseSession.bind(this);
  }

  componentDidMount(){
    this.getsessions();
  }

  chooseSession(e){
    const session = {target: {name: "sessionId", value: e.target.name}};
    const cost = {target: {name: "cost", value: e.target.value}};
    this.props.handleInput(session);
    this.props.handleInput(cost);
  }

  getsessions(){
    getSessions(this.props.user.token)
      .then(json => {
        setTimeout(() => {
          console.log(json);
          if (!json.data.Response){
            this.setState({ active_sessions: json.data.ActiveSessionsList });
          }
          else
            this.props.handleInput({target: {name: "error", value: json.data.Response }});
        }, 0)
      })
      .catch(err =>{
        let handler = new AppiErrorHandler(err);
        let txt = handler.getMessage();
        if(txt !== null){
          if(handler.getError() !== null) txt = txt + '\n' + handler.getError();
        }else txt = handler.getError();
        this.props.handleInput({target: {name: "error", value: txt }});
      });
  }

  render(){
    return(
      <div className="Sessions">
        <h5>Active Sessions</h5>
        {this.state.active_sessions.map((item,i) =>
          <button
            key={i}
            type="button"
            name={item.SessionID}
            className="btn waves-effect waves-light"
            value={item.CurrentCost}
            onClick={this.chooseSession}
          > SessionID: {item.SessionID}, Cost: {item.CurrentCost}
          </button>
        )}
        <button
          type="button"
          name="Refresh"
          className="btn waves-effect waves-light btn_refresh"
          onClick={this.getsessions}
        > Refresh
        </button>
      </div>
    );
  }
}

export default ActiveSessions;
