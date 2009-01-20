                var attributes = {  code:             '${code}'             ,
                                    archive:          '${archive}'          ,
                                    width:            '${width}'            ,
                                    height:           '${height}'           };
                var parameters = {  SmartCardConfig:  '${smartCardConfig}'  ,
                                    ServletPath:      '${servletPath}'      ,
                                    TargetPath:       '${targetPath}'       ,
                                    HelpdeskEventPath:'${helpdeskEventPath}',
                                    HelpPath:         '${helpPath}'         ,
                                    NoPkcs11Path:     '${noPkcs11Path}'     ,
                                    SessionId:        '${sessionId}'        ,
                                    ApplicationId:    '${applicationId}'    ,
                                    Language:         '${language}'         ,
                                    User:             '${userId}'           ,
                                    Operation:        '${operation}'        };
                var version = '${javaVersion}';
                
                deployJava.runApplet(attributes, parameters, version);