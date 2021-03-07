import axios from 'axios';
import config from './config'
import qs from 'qs';

// Here we declare all the api calls to the backend

// some constant declarations to the axios framework
axios.defaults.baseURL = config.apiUrl;
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';

// call to login and get user data like token
// user is the user object to login
export const postLoginUser = user => {
  const url = '/login';
  const data = qs.stringify(user);
  return axios.post(url, data);
}

// call to logout and release token
// obj is the token of the user to disconect
export const postLogout = obj => {
  const url = '/logout';
  const config = {
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj
    }
  };
  return axios.post(url, null, config);
}

// call to healthcheck
// no argument needed
export const getHealthcheck = () => {
  const url = '/admin/healthcheck';
  return axios.get(url);
}

// call to find stations nearby
// obj will contain: token, latitude, longitude, radius
export const getStationsNearby = obj => {
  const url = '/StationsNearby/' + obj.latitude + '/' + obj.longitude + '/' + obj.radius;
  return axios.get(url);
}

// call to get protocol-cost tuples
// obj will contain: token, vehicle, station_point
export const getSessionCost = obj => {
  const url = '/SessionCost/'+obj.vehicle+'/'+obj.station_point;
  const config = {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj.token
    }
  };
  return axios.get(url, config);
}

// call to start charging session
// obj will contain: token, vehicle, station_point, cost or amount
export const startSession = obj => {
  const url1 = '/StartSessionCost/'+obj.vehicle+'/'+obj.station_point+'/'+obj.cost;
  const url2 = '/StartSessionAmount/'+obj.vehicle+'/'+obj.station_point+'/'+obj.amount;
  const config = {
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj.token
    }
  };
  const url = obj.cost ? url1 : url2;
  return axios.post(url, null, config);
}

// call to find stations and points of a stations operator user
export const getStationShow = user => {
  const url = '/Operator/StationShow/' + user.username;
  const config = {
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + user.token
    }
  };
  return axios.get(url, config);
}

const yyyymmdd = date => {
  var mm = date.getMonth() + 1; // getMonth() is zero-based
  var dd = date.getDate();
  return [date.getFullYear(),
          (mm>9 ? '' : '0') + mm,
          (dd>9 ? '' : '0') + dd
         ].join('');
}

// call to find charging sessions per station id
// obj holds station id, date from, date to, operator token
export const getSessionsPerStation = obj => {
  const url = 'SessionsPerStation/' + obj.StationId + '/' + yyyymmdd(obj.fDate) + '/' + yyyymmdd(obj.tDate);
  const config = {
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj.token
    }
  };
  return axios.get(url, config);
}

// call to find charging sessions per point id
// obj holds station id, point id, date from, date to, operator token
export const getSessionsPerPoint = obj => {
  const url = 'SessionsPerPoint/' + obj.StationId + '_' + obj.PointId + '/' + yyyymmdd(obj.fDate) + '/' + yyyymmdd(obj.tDate);
  const config = {
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj.token
    }
  };
  return axios.get(url, config);
}

// call to find the electric vehicles of a user
export const getEvPerUser = user => {
  const url = 'evPerUser/' + user.username;
  const config = {
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + user.token
    }
  };
  return axios.get(url, config);
}

// call to find charging sessions per ev id
// obj holds ev id, date from, date to, operator token
export const getSessionsPerEv = obj => {
  const url = 'SessionsPerEV/' + obj.EvId + '/' + yyyymmdd(obj.fDate) + '/' + yyyymmdd(obj.tDate);
  const config = {
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj.token
    }
  };
  return axios.get(url, config);
}

export const getSessions = obj => {
  const url = '/ActiveSession';
  const config = {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj
    }
  };
  return axios.get(url, config);
}

export const checkout = obj => {
  const url = '/CheckOut/'+obj.sessionId+'?end='+obj.time;
  const config = {
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj.token
    }
  };
  return axios.post(url, null, config);
}

export const showStation = obj => {
  const url = 'Operator/StationShow/'+obj.operator;
  const config = {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj.token
    }
  };
  return axios.get(url, config);
}

export const addStation = obj => {
  const url = 'Operator/StationAdd';
  const params = obj.info;
  const config = {
    params: params,
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj.token
    }
  };
  return axios.post(url, null, config);
}

export const removeStation = obj => {
  const url = 'Operator/StationRemove/'+obj.station;
  const config = {
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj.token
    }
  };
  return axios.post(url, null, config);
}

export const creditCardPayment = obj => {
  const url = 'CreditCardPayment/'+obj.sessionId;
  const config = {
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj.token
    }
  };
  return axios.post(url, null, config);
}
