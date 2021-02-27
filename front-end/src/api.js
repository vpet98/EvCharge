import axios from 'axios';
import config from './config'
import qs from 'qs';

// Here we declare all the api calls to the backend

// some constant declarations to the axios framework
axios.defaults.baseURL = config.apiUrl;
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';

// call to login and get token
// obj is the user object to login
export const postLoginToken = obj => {
  const url = '/login';
  const data = qs.stringify(obj);
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
  const config = {
    headers: {
      'X-OBSERVATORY-AUTH': 'Bearer ' + obj.token
    }
  };
  return axios.get(url, null, config);
}
