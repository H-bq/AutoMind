import http from './http'

export const getTopologyOverview = () => http.get('/topology/overview')
export const getGlobalTopology = () => http.get('/topology/global')
export const getDomainTopology = (domainId) => http.get(`/topology/domain/${domainId}`)
export const getLinkStatus = (linkId) => http.get(`/topology/link/${linkId}/status`)
export const listDomains = () => http.get('/network-domain/list')
export const listNodes = () => http.get('/network-node/list')
export const listLinks = () => http.get('/network-link/list')
export const listLinkResources = () => http.get('/network-link-resource/list')
